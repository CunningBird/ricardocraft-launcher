package ru.ricardocraft.backend.command.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.binary.LauncherBinary;
import ru.ricardocraft.backend.binary.tasks.SignJarTask;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Component
public class SignJarCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(SignJarCommand.class);

    private transient final LaunchServerProperties config;
    private transient final DirectoriesManager directoriesManager;
    private transient final LauncherBinary launcherBinary;

    @Autowired
    public SignJarCommand(LaunchServerProperties config,
                          DirectoriesManager directoriesManager,
                          JarLauncherBinary launcherBinary) {
        super();
        this.config = config;
        this.directoriesManager = directoriesManager;
        this.launcherBinary = launcherBinary;
    }

    @Override
    public String getArgsDescription() {
        return "[path to file] (path to signed file)";
    }

    @Override
    public String getUsageDescription() {
        return "sign custom jar";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        Path target = Paths.get(args[0]);
        Path tmpSign;
        if (args.length > 1)
            tmpSign = Paths.get(args[1]);
        else
            tmpSign = directoriesManager.getBuildDir().resolve(target.toFile().getName());
        logger.info("Signing jar {} to {}", target.toString(), tmpSign.toString());
        Optional<SignJarTask> task = launcherBinary.getTaskByClass(SignJarTask.class);
        if (task.isEmpty()) throw new IllegalStateException("SignJarTask not found");
        task.get().sign(config.getSign(), target, tmpSign);
        if (args.length == 1) {
            logger.info("Move temp jar {} to {}", tmpSign.toString(), target.toString());
            Files.deleteIfExists(target);
            Files.move(tmpSign, target);
        }
        logger.info("Success signed");
    }
}
