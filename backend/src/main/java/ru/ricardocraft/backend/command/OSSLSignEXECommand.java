package ru.ricardocraft.backend.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.binary.tasks.OSSLSignTask;
import ru.ricardocraft.backend.command.utls.CommandHandler;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OSSLSignEXECommand extends Command {

    public final LaunchServerConfig config;

    @Autowired
    public OSSLSignEXECommand(CommandHandler commandHandler, LaunchServerConfig config) {
        super();
        this.config = config;

        commandHandler.registerCommand("osslsignexe", this);
    }

    @Override
    public String getArgsDescription() {
        return "[path to input exe] [path to output exe]";
    }

    @Override
    public String getUsageDescription() {
        return "sign launch4j exe";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        Path inputPath = Paths.get(args[0]);
        Path outputPath = Paths.get(args[1]);
        OSSLSignTask.signLaunch4j(config.sign.osslSignCodeConfig, config.sign, inputPath, outputPath);
    }
}
