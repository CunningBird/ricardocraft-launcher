package ru.ricardocraft.bff.command.basic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.command.Command;
import ru.ricardocraft.bff.utils.Version;
import ru.ricardocraft.bff.helper.JVMHelper;

import java.lang.management.RuntimeMXBean;

public final class VersionCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    public VersionCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Print LaunchServer version";
    }

    @Override
    public void invoke(String... args) {
        logger.info("LaunchServer version: {}.{}.{} (build #{})", Version.MAJOR, Version.MINOR, Version.PATCH, Version.BUILD);
        RuntimeMXBean mxBean = JVMHelper.RUNTIME_MXBEAN;
        logger.info("Java {}({})", JVMHelper.getVersion(), mxBean.getVmVersion());
        logger.info("Java Home: {}", System.getProperty("java.home", "UNKNOWN"));
    }
}
