package ru.ricardocraft.backend.command;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.binary.tasks.OSSLSignTask;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OSSLSignEXECommand extends Command {
    public final LaunchServerConfig.OSSLSignCodeConfig config;

    public OSSLSignEXECommand(LaunchServer server) {
        super(server);
        this.config = server.config.osslSignCodeConfig;
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
        OSSLSignTask.signLaunch4j(config, server.config.sign, inputPath, outputPath);
    }
}
