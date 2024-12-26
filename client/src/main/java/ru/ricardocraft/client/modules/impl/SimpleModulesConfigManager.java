package ru.ricardocraft.client.modules.impl;

import ru.ricardocraft.client.modules.ModulesConfigManager;

import java.nio.file.Path;

public class SimpleModulesConfigManager implements ModulesConfigManager {

    public final Path configDir;

    public SimpleModulesConfigManager(Path configDir) {
        this.configDir = configDir;
    }

}
