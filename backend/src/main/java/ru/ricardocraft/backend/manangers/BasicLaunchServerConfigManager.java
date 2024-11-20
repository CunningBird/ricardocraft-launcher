package ru.ricardocraft.backend.manangers;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;

@Component
public class BasicLaunchServerConfigManager implements LaunchServerConfigManager {

    private final Path configFile;
    private final JacksonManager jacksonManager;

    public BasicLaunchServerConfigManager(JacksonManager jacksonManager) {
        this.configFile = IOHelper.WORKING_DIR.resolve("LaunchServer.json");
        this.jacksonManager = jacksonManager;
    }

    @Override
    public LaunchServerConfig readConfig() throws IOException {
        LaunchServerConfig config1;
        try (BufferedReader reader = IOHelper.newReader(configFile)) {
            config1 = jacksonManager.getMapper().readValue(reader, LaunchServerConfig.class);
        }
        return config1;
    }

    @Override
    public void writeConfig(LaunchServerConfig config) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Writer writer = IOHelper.newWriter(output)) {
            writer.write(jacksonManager.getMapper().writeValueAsString(config));
        }
        byte[] bytes = output.toByteArray();
        if(bytes.length > 0) {
            IOHelper.write(configFile, bytes);
        }
    }
}
