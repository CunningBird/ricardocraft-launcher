package ru.ricardocraft.bff.command.basic;

import ru.ricardocraft.bff.command.utls.Command;
import ru.ricardocraft.bff.helper.JVMHelper;
import ru.ricardocraft.bff.helper.LogHelper;

public class GCCommand extends Command {
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
