package ru.ricardocraft.backend.command.basic;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.CommandHandler;

@Component
public class DebugCommand extends Command {

    private final transient Logger logger = LogManager.getLogger();

    @Override
    public String getArgsDescription() {
        return "[true/false]";
    }

    @Override
    public String getUsageDescription() {
        return "Enable log level TRACE in LaunchServer";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        boolean value = Boolean.parseBoolean(args[0]);
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("pro.gravit");
        loggerConfig.setLevel(value ? Level.TRACE : Level.DEBUG);
        ctx.updateLoggers();
        if (value) {
            logger.info("Log level TRACE enabled");
        } else {
            logger.info("Log level TRACE disabled");
        }
    }
}
