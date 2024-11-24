package ru.ricardocraft.client.base.modules.impl;

import ru.ricardocraft.client.base.modules.LauncherModulesContext;
import ru.ricardocraft.client.base.modules.LauncherModulesManager;
import ru.ricardocraft.client.base.modules.ModulesConfigManager;

public class SimpleModuleContext implements LauncherModulesContext {
    public final LauncherModulesManager modulesManager;
    public final ModulesConfigManager configManager;

    public SimpleModuleContext(LauncherModulesManager modulesManager, ModulesConfigManager configManager) {
        this.modulesManager = modulesManager;
        this.configManager = configManager;
    }

    @Override
    public LauncherModulesManager getModulesManager() {
        return modulesManager;
    }

    @Override
    public ModulesConfigManager getModulesConfigManager() {
        return configManager;
    }
}
