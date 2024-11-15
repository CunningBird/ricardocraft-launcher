package ru.ricardocraft.backend.command.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.JVMHelper;

@Component
public class ServerStatusCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Check server status";
    }

    @Override
    public void invoke(String... args) {
        logger.info("Show server status");
        logger.info("Memory: free {} | total: {} | max: {}", JVMHelper.RUNTIME.freeMemory(), JVMHelper.RUNTIME.totalMemory(), JVMHelper.RUNTIME.maxMemory());
        long uptime = JVMHelper.RUNTIME_MXBEAN.getUptime() / 1000;
        long second = uptime % 60;
        long min = (uptime / 60) % 60;
        long hour = (uptime / 60 / 60) % 24;
        long days = (uptime / 60 / 60 / 24);
        logger.info("Uptime: {} days {} hours {} minutes {} seconds", days, hour, min, second);
        logger.info("Uptime (double): {}", (double) JVMHelper.RUNTIME_MXBEAN.getUptime() / 1000);
    }
}
