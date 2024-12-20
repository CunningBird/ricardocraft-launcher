package ru.ricardocraft.backend.command.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.binary.LauncherBinary;
import ru.ricardocraft.backend.binary.tasks.SignJarTask;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;

@Component
public class SignDirCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(SignDirCommand.class);

    private transient final LaunchServerProperties config;
    private transient final DirectoriesManager directoriesManager;
    private transient final LauncherBinary launcherBinary;

    @Autowired
    public SignDirCommand(LaunchServerProperties config,
                          DirectoriesManager directoriesManager,
                          JarLauncherBinary launcherBinary) {
        super();
        this.config = config;
        this.directoriesManager = directoriesManager;
        this.launcherBinary = launcherBinary;
    }

    @Override
    public String getArgsDescription() {
        return "[path to dir]";
    }

    @Override
    public String getUsageDescription() {
        return "sign all jar files into dir";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        Path targetDir = Paths.get(args[0]);
        if (!IOHelper.isDir(targetDir))
            throw new IllegalArgumentException("%s not directory".formatted(targetDir));
        Optional<SignJarTask> task = launcherBinary.getTaskByClass(SignJarTask.class);
        if (task.isEmpty()) throw new IllegalStateException("SignJarTask not found");
        IOHelper.walk(targetDir, new SignJarVisitor(task.get()), true);
        logger.info("Success signed");
    }

    private class SignJarVisitor extends SimpleFileVisitor<Path> {
        private final SignJarTask task;

        public SignJarVisitor(SignJarTask task) {
            this.task = task;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toFile().getName().endsWith(".jar")) {
                Path tmpSign = directoriesManager.getBuildDir().resolve(file.toFile().getName());
                logger.info("Signing jar {}", file.toString());
                task.sign(config.getSign(), file, tmpSign);
                Files.deleteIfExists(file);
                Files.move(tmpSign, file);
            }
            return super.visitFile(file, attrs);
        }
    }
}
