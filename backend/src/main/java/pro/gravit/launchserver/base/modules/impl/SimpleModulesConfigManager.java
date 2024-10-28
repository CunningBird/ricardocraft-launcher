package pro.gravit.launchserver.base.modules.impl;

import pro.gravit.launchserver.base.config.SimpleConfigurable;
import pro.gravit.launchserver.base.modules.ModulesConfigManager;
import pro.gravit.launchserver.utils.helper.IOHelper;
import pro.gravit.launchserver.utils.helper.LogHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleModulesConfigManager implements ModulesConfigManager {
    public final Path configDir;

    public SimpleModulesConfigManager(Path configDir) {
        this.configDir = configDir;
    }

    public Path getModuleConfig(String moduleName) {
        return getModuleConfig(moduleName, "Config");
    }

    @Override
    public Path getModuleConfig(String moduleName, String configName) {
        return getModuleConfigDir(moduleName).resolve(configName.concat(".json"));
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

    @Override
    public <T> SimpleConfigurable<T> getConfigurable(Class<T> tClass, Path configPath) {
        return new SimpleConfigurable<>(tClass, configPath);
    }
}
