package ru.ricardocraft.client.commands.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.commands.Command;
import ru.ricardocraft.client.commands.CommandHandler;
import ru.ricardocraft.client.base.helper.JVMHelper;
import ru.ricardocraft.client.base.helper.LogHelper;

@Component
public class GCCommand extends Command {

    @Autowired
    public GCCommand(CommandHandler commandHandler) {
        commandHandler.registerCommand("gc", this);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return null;
    }

    @Override
    public void invoke(String... args) {
        LogHelper.subInfo("Performing full GC");
        JVMHelper.fullGC();
        // Print memory usage
        long max = JVMHelper.RUNTIME.maxMemory() >> 20;
        long free = JVMHelper.RUNTIME.freeMemory() >> 20;
        long total = JVMHelper.RUNTIME.totalMemory() >> 20;
        long used = total - free;
        LogHelper.subInfo("Heap usage: %d / %d / %d MiB", used, total, max);
    }
}
