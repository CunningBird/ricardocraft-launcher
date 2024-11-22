package ru.ricardocraft.backend.command.basic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.command.Command;

@Component
public final class BuildCommand extends Command {

    public final JarLauncherBinary launcherBinary;
    public final EXELauncherBinary launcherEXEBinary;

    @Autowired
    public BuildCommand(JarLauncherBinary launcherBinary, EXELauncherBinary launcherEXEBinary) {
        super();
        this.launcherBinary = launcherBinary;
        this.launcherEXEBinary = launcherEXEBinary;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Build launcher binaries";
    }

    @Override
    public void invoke(String... args) throws Exception {
        launcherBinary.build();
        launcherEXEBinary.build();

        launcherBinary.check();
        launcherEXEBinary.check();
    }
}
