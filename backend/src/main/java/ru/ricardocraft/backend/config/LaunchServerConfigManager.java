package ru.ricardocraft.backend.config;

import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerRuntimeConfig;

import java.io.IOException;

public interface LaunchServerConfigManager {
    LaunchServerConfig readConfig() throws IOException;

    LaunchServerRuntimeConfig readRuntimeConfig() throws IOException;

    void writeConfig(LaunchServerConfig config) throws IOException;

    void writeRuntimeConfig(LaunchServerRuntimeConfig config) throws IOException;
}