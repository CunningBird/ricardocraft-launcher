package ru.ricardocraft.backend.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.ricardocraft.backend.base.Version;
import ru.ricardocraft.backend.base.helper.JVMHelper;

import java.lang.management.RuntimeMXBean;

import static ru.ricardocraft.backend.base.helper.JVMHelper.RUNTIME;

@Slf4j
@ShellComponent("basic")
public class BasicCommands {

    @ShellMethod("Invoke Garbage Collector")
    public void gc() {
        log.info("Performing full GC");
        RUNTIME.gc();
        log.debug("Used heap: {} MiB", RUNTIME.totalMemory() - RUNTIME.freeMemory() >> 20);
        // Print memory usage
        long max = RUNTIME.maxMemory() >> 20;
        long free = RUNTIME.freeMemory() >> 20;
        long total = RUNTIME.totalMemory() >> 20;
        long used = total - free;
        log.info("Heap usage: {} / {} / {} MiB", used, total, max);
    }

    @ShellMethod("Stop LaunchServer")
    public void stop() {
        JVMHelper.RUNTIME.exit(0);
    }

    @ShellMethod("Print LaunchServer version")
    public void launcherVersion() {
        log.info("LaunchServer version: {}.{}.{} (build #{})", Version.MAJOR, Version.MINOR, Version.PATCH, Version.BUILD);
        RuntimeMXBean mxBean = JVMHelper.RUNTIME_MXBEAN;
        log.info("Java {}({})", Runtime.version().feature(), mxBean.getVmVersion());
        log.info("Java Home: {}", System.getProperty("java.home", "UNKNOWN"));
    }
}
