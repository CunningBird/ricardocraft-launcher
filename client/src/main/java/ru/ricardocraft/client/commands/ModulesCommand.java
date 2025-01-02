package ru.ricardocraft.client.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.service.modules.LauncherModule;
import ru.ricardocraft.client.service.modules.LauncherModuleInfo;
import ru.ricardocraft.client.service.launch.RuntimeModuleManager;
import ru.ricardocraft.client.base.helper.LogHelper;

@Component
public class ModulesCommand extends Command {

    private final RuntimeModuleManager modulesManager;

    @Autowired
    public ModulesCommand(RuntimeModuleManager modulesManager, CommandHandler commandHandler) {
        this.modulesManager = modulesManager;
        commandHandler.registerCommand("modules", this);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "show modules";
    }

    @Override
    public void invoke(String... args) {
        for (LauncherModule module : modulesManager.getModules()) {
            LauncherModuleInfo info = module.getModuleInfo();
            LogHelper.info("[MODULE] %s v: %s", info.name, info.version.getVersionString());
        }
    }
}
