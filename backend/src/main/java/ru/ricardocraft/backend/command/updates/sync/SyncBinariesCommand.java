package ru.ricardocraft.backend.command.updates.sync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.command.Command;

import java.io.IOException;

@Component
public final class SyncBinariesCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(SyncBinariesCommand.class);

    private transient final JARLauncherBinary launcherBinary;
    private transient final EXELauncherBinary exeLauncherBinary;

    @Autowired
    public SyncBinariesCommand(JARLauncherBinary launcherBinary, EXELauncherBinary exeLauncherBinary) {
        super();
        this.launcherBinary = launcherBinary;
        this.exeLauncherBinary = exeLauncherBinary;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Resync launcher binaries";
    }

    @Override
    public void invoke(String... args) throws IOException {
        // Syncing launcher binary
        logger.info("Syncing launcher binary file");
        if (!launcherBinary.sync()) logger.warn("Missing launcher binary file");

        // Syncing launcher EXE binary
        logger.info("Syncing launcher EXE binary file");
        if (!exeLauncherBinary.sync()) logger.warn("Missing launcher EXE binary file");
        logger.info("Binaries successfully resynced");
    }
}
