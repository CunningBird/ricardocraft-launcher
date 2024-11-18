package ru.ricardocraft.backend.command.basic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.base.utils.Version;
import ru.ricardocraft.backend.command.Command;

import java.lang.management.RuntimeMXBean;

@Component
public final class VersionCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(VersionCommand.class);

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
