package ru.ricardocraft.client.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.modules.LauncherModule;
import ru.ricardocraft.client.modules.LauncherModuleInfo;
import ru.ricardocraft.client.launch.RuntimeModuleManager;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.command.CommandHandler;
import ru.ricardocraft.client.utils.helper.LogHelper;

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
