package ru.ricardocraft.backend.binary;

import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.binary.tasks.LauncherBuildTask;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public abstract class LauncherBinary extends BinaryPipeline {
    public final UpdatesProvider updatesProvider;
    public final Path syncBinaryFile;
    private volatile byte[] digest;

    protected LauncherBinary(LaunchServerDirectories directories,
                             UpdatesProvider updatesProvider,
                             Path binaryFile,
                             String nameFormat) {
        super(directories.tmpDir.resolve("build"), nameFormat);
        this.updatesProvider = updatesProvider;
        syncBinaryFile = binaryFile;
    }

    public static Path resolve(LaunchServerProperties properties, String ext) {
        return Path.of(properties.getBinaryName() + ext);
    }

    public void build() throws IOException {
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
}
