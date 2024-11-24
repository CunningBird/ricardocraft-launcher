package ru.ricardocraft.client.base.modules.impl;

import ru.ricardocraft.client.base.modules.ModulesConfigManager;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleModulesConfigManager implements ModulesConfigManager {

    public final Path configDir;

    public SimpleModulesConfigManager(Path configDir) {
        this.configDir = configDir;
    }

    public Path getModuleConfigDir(String moduleName) {
        if (!IOHelper.isDir(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                LogHelper.error(e);
            }
        }
        return configDir.resolve(moduleName);
    }
}
