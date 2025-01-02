package ru.ricardocraft.client.service.modules.impl;

import ru.ricardocraft.client.service.modules.ModulesConfigManager;

import java.nio.file.Path;

public class SimpleModulesConfigManager implements ModulesConfigManager {

    public final Path configDir;

    public SimpleModulesConfigManager(Path configDir) {
        this.configDir = configDir;
    }

}
