package ru.ricardocraft.backend.command.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.command.utls.Command;

@Component
public class GCCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(GCCommand.class);

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
        logger.info("Performing full GC");
        JVMHelper.fullGC();
        // Print memory usage
        long max = JVMHelper.RUNTIME.maxMemory() >> 20;
        long free = JVMHelper.RUNTIME.freeMemory() >> 20;
        long total = JVMHelper.RUNTIME.totalMemory() >> 20;
        long used = total - free;
        logger.info("Heap usage: {} / {} / {} MiB", used, total, max);
    }
}
