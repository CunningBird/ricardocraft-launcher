package ru.ricardocraft.client.base.modules;

import java.nio.file.Path;

public interface ModulesConfigManager {

    Path getModuleConfigDir(String moduleName);

}
