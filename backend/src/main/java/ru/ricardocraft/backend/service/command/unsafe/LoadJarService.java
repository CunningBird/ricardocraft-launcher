package ru.ricardocraft.backend.service.command.unsafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

@Slf4j
@Component
public class LoadJarService {

    public void sendAuth(String jarFile) throws Exception {
        Path file = Paths.get(jarFile);
        StarterAgent.inst.appendToSystemClassLoaderSearch(new JarFile(file.toFile()));
        log.info("File {} added to system classpath", file.toAbsolutePath());
    }

    public static final class StarterAgent {

        public static Instrumentation inst = null;
        public static Path libraries = null;

        public static void premain(String agentArgument, Instrumentation inst) {
            throw new UnsupportedOperationException("Please remove -javaagent option from start.sh");
        }
    }
}
