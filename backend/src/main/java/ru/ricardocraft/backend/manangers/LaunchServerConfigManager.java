package ru.ricardocraft.backend.manangers;

import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.io.IOException;

public interface LaunchServerConfigManager {
    LaunchServerConfig readConfig() throws IOException;

    void writeConfig(LaunchServerConfig config) throws IOException;
}