package ru.ricardocraft.backend.command.unsafe;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.StarterAgent;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.helper.LogHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

@Component
@NoArgsConstructor
public class LoadJarCommand extends Command {

    @Override
    public String getArgsDescription() {
        return "[jarfile]";
    }

    @Override
    public String getUsageDescription() {
        return "Load jar file";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        Path file = Paths.get(args[0]);
        StarterAgent.inst.appendToSystemClassLoaderSearch(new JarFile(file.toFile()));
        LogHelper.info("File %s added to system classpath", file.toAbsolutePath().toString());
    }
}