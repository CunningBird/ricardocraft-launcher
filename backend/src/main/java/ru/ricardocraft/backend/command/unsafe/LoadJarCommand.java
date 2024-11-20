package ru.ricardocraft.backend.command.unsafe;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

@Component
@NoArgsConstructor
public class LoadJarCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(LoadJarCommand.class);

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
        logger.info("File {} added to system classpath", file.toAbsolutePath());
    }

    public static final class StarterAgent {

        public static Instrumentation inst = null;
        public static Path libraries = null;

        public static void premain(String agentArgument, Instrumentation inst) {
            throw new UnsupportedOperationException("Please remove -javaagent option from start.sh");
        }
    }
}
