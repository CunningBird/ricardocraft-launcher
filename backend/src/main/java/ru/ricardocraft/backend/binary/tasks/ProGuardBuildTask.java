package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.binary.JarLauncherInfo;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.ProguardConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProGuardBuildTask implements LauncherBuildTask {

    private transient final Logger logger = LogManager.getLogger(ProGuardBuildTask.class);

    public static final String[] JAVA9_OPTS = new String[]{
            "-libraryjars '<java.home>/jmods/'"
    };

    private final JarLauncherInfo jarLauncherInfo;
    private final LaunchServerProperties properties;
    private final DirectoriesManager directoriesManager;
    private final JarLauncherBinary launcherBinary;
    private final ProguardConfig proguardConf;

    public ProGuardBuildTask(JarLauncherInfo jarLauncherInfo,
                             JarLauncherBinary launcherBinary,
                             LaunchServerProperties properties,
                             DirectoriesManager directoriesManager,
                             ProguardConfig conf) {
        this.properties = properties;
        this.directoriesManager = directoriesManager;
        this.launcherBinary = launcherBinary;
        this.proguardConf = conf;
        this.jarLauncherInfo = jarLauncherInfo;
    }

    @Override
    public String getName() {
        return "ProGuard.proguard";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        Path outputJar = launcherBinary.nextLowerPath(this);
        if (properties.getProguard().getEnabled()) {
            if (!checkJMods(IOHelper.JVM_DIR.resolve("jmods"))) {
                throw new RuntimeException("Java path: %s is not JDK! Please install JDK".formatted(IOHelper.JVM_DIR));
            }
            Path jfxPath = tryFindOpenJFXPath(IOHelper.JVM_DIR);
            if (checkFXJMods(IOHelper.JVM_DIR.resolve("jmods"))) {
                logger.debug("JavaFX jmods resolved in JDK path");
                jfxPath = null;
            } else if (jfxPath != null && checkFXJMods(jfxPath)) {
                logger.debug("JMods resolved in {}", jfxPath.toString());
            } else {
                throw new RuntimeException("JavaFX jmods not found. May be install OpenJFX?");
            }
            try {
                List<String> args = new ArrayList<>();
                args.add(IOHelper.resolveJavaBin(IOHelper.JVM_DIR).toAbsolutePath().toString());
                args.addAll(properties.getProguard().getJvmArgs());
                args.add("-cp");
                try (Stream<Path> files = Files.walk(directoriesManager.getLibrariesDir(), FileVisitOption.FOLLOW_LINKS)) {
                    args.add(files
                            .filter(e -> e.getFileName().toString().endsWith(".jar"))
                            .map(path -> path.toAbsolutePath().toString())
                            .collect(Collectors.joining(File.pathSeparator))
                    );
                }
                args.add("proguard.ProGuard");
                buildConfig(args, inputFile, outputJar, jfxPath == null ? new Path[0] : new Path[]{jfxPath});

                Process process = new ProcessBuilder()
                        .command(args)
                        .inheritIO()
                        .directory(proguardConf.proguard.toFile())
                        .start();

                try {
                    process.waitFor();
                } catch (InterruptedException ignored) {

                }
                if (process.exitValue() != 0) {
                    throw new RuntimeException("ProGuard process return %d".formatted(process.exitValue()));
                }
            } catch (Exception e) {
                logger.error(e);
            }
        } else
            IOHelper.copy(inputFile, outputJar);
        return outputJar;
    }

    private boolean checkJMods(Path path) {
        return IOHelper.exists(path.resolve("java.base.jmod"));
    }

    public Path tryFindOpenJFXPath(Path jvmDir) {
        String dirName = jvmDir.getFileName().toString();
        Path parent = jvmDir.getParent();
        if (parent == null) return null;
        Path archJFXPath = parent.resolve(dirName.replace("openjdk", "openjfx")).resolve("jmods");
        if (Files.isDirectory(archJFXPath)) {
            return archJFXPath;
        }
        Path arch2JFXPath = parent.resolve(dirName.replace("jdk", "openjfx")).resolve("jmods");
        if (Files.isDirectory(arch2JFXPath)) {
            return arch2JFXPath;
        }
        if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
            Path debianJfxPath = Paths.get("/usr/share/openjfx/jmods");
            if (Files.isDirectory(debianJfxPath)) {
                return debianJfxPath;
            }
        }
        return null;
    }

    public boolean checkFXJMods(Path path) {
        if (!IOHelper.exists(path.resolve("javafx.base.jmod")))
            return false;
        if (!IOHelper.exists(path.resolve("javafx.graphics.jmod")))
            return false;
        return IOHelper.exists(path.resolve("javafx.controls.jmod"));
    }

    public void buildConfig(List<String> confStrs, Path inputJar, Path outputJar, Path[] jfxPath) {
        proguardConf.prepare(false);
        if (properties.getProguard().getMappings())
            confStrs.add("-printmapping '" + proguardConf.mappings.toFile().getName() + "'");
        confStrs.add("-obfuscationdictionary '" + proguardConf.words.toFile().getName() + "'");
        confStrs.add("-injar '" + inputJar.toAbsolutePath() + "'");
        confStrs.add("-outjar '" + outputJar.toAbsolutePath() + "'");
        Collections.addAll(confStrs, JAVA9_OPTS);
        if (jfxPath != null) {
            for (Path path : jfxPath) {
                confStrs.add("-libraryjars '%s'".formatted(path.toAbsolutePath()));
            }
        }
        jarLauncherInfo.getCoreLibs().stream()
                .map(e -> "-libraryjars '" + e.toAbsolutePath() + "'")
                .forEach(confStrs::add);

        jarLauncherInfo.getAddonLibs().stream()
                .map(e -> "-libraryjars '" + e.toAbsolutePath() + "'")
                .forEach(confStrs::add);
        confStrs.add("-classobfuscationdictionary '" + proguardConf.words.toFile().getName() + "'");
        confStrs.add("@".concat(proguardConf.config.toFile().getName()));
    }
}
