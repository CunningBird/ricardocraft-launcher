package ru.ricardocraft.backend.manangers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

public class BasicLaunchServerConfigManager implements LaunchServerConfigManager {

    private static final Logger logger = LogManager.getLogger();

    private final Path configFile;
    private final Path runtimeConfigFile;

    public BasicLaunchServerConfigManager(Path configFile, Path runtimeConfigFile) {
        this.configFile = configFile;
        this.runtimeConfigFile = runtimeConfigFile;
    }

    @Override
    public LaunchServerConfig readConfig() throws IOException {
        LaunchServerConfig config1;
        try (BufferedReader reader = IOHelper.newReader(configFile)) {
            config1 = Launcher.gsonManager.gson.fromJson(reader, LaunchServerConfig.class);
        }
        return config1;
    }

    @Override
    public LaunchServerRuntimeConfig readRuntimeConfig() throws IOException {
        LaunchServerRuntimeConfig config1;
        try (BufferedReader reader = IOHelper.newReader(runtimeConfigFile)) {
            config1 = Launcher.gsonManager.gson.fromJson(reader, LaunchServerRuntimeConfig.class);
        }
        return config1;
    }

    @Override
    public void writeConfig(LaunchServerConfig config) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Writer writer = IOHelper.newWriter(output)) {
            if (Launcher.gsonManager.configGson != null) {
                Launcher.gsonManager.configGson.toJson(config, writer);
            } else {
                logger.error("Error writing LaunchServer config file. Gson is null");
            }
        }
        byte[] bytes = output.toByteArray();
        if(bytes.length > 0) {
            IOHelper.write(configFile, bytes);
        }
    }

    @Override
    public void writeRuntimeConfig(LaunchServerRuntimeConfig config) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Writer writer = IOHelper.newWriter(output)) {
            if (Launcher.gsonManager.configGson != null) {
                Launcher.gsonManager.configGson.toJson(config, writer);
            } else {
                logger.error("Error writing LaunchServer runtime config file. Gson is null");
            }
        }
        byte[] bytes = output.toByteArray();
        if(bytes.length > 0) {
            IOHelper.write(runtimeConfigFile, bytes);
        }
    }
}
