package ru.ricardocraft.backend.command.unsafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarFile;

@Slf4j
@ShellComponent
@ShellCommandGroup("unsafe")
public class LoadJarCommand {

    @ShellMethod("[jarfile] Load jar file")
    public void sendAuth(@ShellOption String jarFile) throws Exception {
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
