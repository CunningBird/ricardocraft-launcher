package ru.ricardocraft.client.commands;

import ru.ricardocraft.client.base.modules.JavaRuntimeModule;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class VersionCommand extends Command {
    @Override
    public String getArgsDescription() {
        return "print version information";
    }

    @Override
    public String getUsageDescription() {
        return "[]";
    }

    @Override
    public void invoke(String... args) {
        LogHelper.info(JavaRuntimeModule.getLauncherInfo());
        LogHelper.info("JDK Path: %s", System.getProperty("java.home", "UNKNOWN"));
    }
}
