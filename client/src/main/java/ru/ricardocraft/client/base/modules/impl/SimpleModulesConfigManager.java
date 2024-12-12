package ru.ricardocraft.client.base.modules.impl;

import ru.ricardocraft.client.base.modules.ModulesConfigManager;

import java.nio.file.Path;

public class SimpleModulesConfigManager implements ModulesConfigManager {

    public final Path configDir;

    public SimpleModulesConfigManager(Path configDir) {
        this.configDir = configDir;
    }

}
