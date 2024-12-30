package ru.ricardocraft.backend.service.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.dto.Version;

import java.lang.management.RuntimeMXBean;

import static ru.ricardocraft.backend.base.helper.JVMHelper.RUNTIME;

@Slf4j
@Component
public class BasicService {

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

    public void launcherVersion() {
        log.info("LaunchServer version: {}.{}.{} (build #{})", Version.MAJOR, Version.MINOR, Version.PATCH, Version.BUILD);
        RuntimeMXBean mxBean = JVMHelper.RUNTIME_MXBEAN;
        log.info("Java {}({})", Runtime.version().feature(), mxBean.getVmVersion());
        log.info("Java Home: {}", System.getProperty("java.home", "UNKNOWN"));
    }
}
