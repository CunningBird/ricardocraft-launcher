package ru.ricardocraft.backend.binary;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.helper.CommonHelper;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.binary.tasks.LauncherBuildTask;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class LauncherBinary {

    protected transient final Logger logger = LogManager.getLogger(LauncherBinary.class);

    protected final List<LauncherBuildTask> tasks = new ArrayList<>();
    protected final Path syncBinaryFile;
    private final Path buildDir;
    private final String nameFormat;
    private final UpdatesProvider updatesProvider;
    private volatile byte[] digest;

    protected LauncherBinary(DirectoriesManager directoriesManager,
                             UpdatesProvider updatesProvider,
                             Path binaryFile,
                             String nameFormat) {
        this.updatesProvider = updatesProvider;
        this.syncBinaryFile = binaryFile;
        this.buildDir = directoriesManager.getTmpDir().resolve("build");
        this.nameFormat = nameFormat;
    }

    public static Path resolve(LaunchServerProperties properties, String ext) {
        return Path.of(properties.getBinaryName() + ext);
    }

    public void build() throws Exception {
        logger.info("Building launcher binary file");
        Path thisPath = null;
        long time_start = System.currentTimeMillis();
        long time_this = time_start;
        for (LauncherBuildTask task : tasks) {
            logger.info("Task {}", task.getName());
            Path oldPath = thisPath;
            thisPath = task.process(oldPath);
            long time_task_end = System.currentTimeMillis();
            long time_task = time_task_end - time_this;
            time_this = time_task_end;
            logger.info("Task {} processed from {} millis", task.getName(), time_task);
        }
        long time_end = System.currentTimeMillis();
        if (thisPath == null) throw new Exception();
        updatesProvider.upload(null, Map.of(syncBinaryFile.toString(), thisPath), true);
        IOHelper.deleteDir(buildDir, false);
        logger.info("Build successful from {} millis", time_end - time_start);
    }

    public final boolean exists() {
        return syncBinaryFile != null && IOHelper.isFile(syncBinaryFile);
    }

    public final byte[] getDigest() {
        return digest;
    }

    public final boolean sync() throws IOException {
        try {
            var target = syncBinaryFile.toString();
            var path = updatesProvider.download(null, List.of(target)).get(target);
            digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA512, IOHelper.read(path));
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public void addCounted(int count, Predicate<LauncherBuildTask> pred, LauncherBuildTask taskAdd) {
        List<LauncherBuildTask> indexes = new ArrayList<>();
        tasks.stream().filter(pred).forEach(indexes::add);
        indexes.forEach(e -> tasks.add(tasks.indexOf(e) + count, taskAdd));
    }

    public void replaceCounted(int count, Predicate<LauncherBuildTask> pred, LauncherBuildTask taskRep) {
        List<LauncherBuildTask> indexes = new ArrayList<>();
        tasks.stream().filter(pred).forEach(indexes::add);
        indexes.forEach(e -> tasks.set(tasks.indexOf(e) + count, taskRep));
    }

    public void add(Predicate<LauncherBuildTask> pred, LauncherBuildTask taskAdd) {
        addCounted(0, pred, taskAdd);
    }

    public void replace(Predicate<LauncherBuildTask> pred, LauncherBuildTask taskRep) {
        replaceCounted(0, pred, taskRep);
    }

    public <T extends LauncherBuildTask> Optional<T> getTaskByClass(Class<T> taskClass) {
        return tasks.stream().filter(taskClass::isInstance).map(taskClass::cast).findFirst();
    }

    public Optional<LauncherBuildTask> getTaskBefore(Predicate<LauncherBuildTask> pred) {
        LauncherBuildTask last = null;
        for (var e : tasks) {
            if (pred.test(e)) {
                return Optional.ofNullable(last);
            }
            last = e;
        }
        return Optional.empty();
    }

    public String nextName(String taskName) {
        return nameFormat.formatted(taskName);
    }

    public Path nextPath(String taskName) {
        return buildDir.resolve(nextName(taskName));
    }

    public Path nextPath(LauncherBuildTask task) {
        return nextPath(task.getName());
    }

    public Path nextLowerPath(LauncherBuildTask task) {
        return nextPath(CommonHelper.low(task.getName()));
    }
}
