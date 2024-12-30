package ru.ricardocraft.backend.base.helper;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

public final class JVMHelper {

    // MXBeans exports
    public static final RuntimeMXBean RUNTIME_MXBEAN = ManagementFactory.getRuntimeMXBean();
    public static final OperatingSystemMXBean OPERATING_SYSTEM_MXBEAN = ManagementFactory.getOperatingSystemMXBean();
    public static final OS OS_TYPE = OS.byName(OPERATING_SYSTEM_MXBEAN.getName());
    public static final Runtime RUNTIME = Runtime.getRuntime();

    static {
        try {
            MethodHandles.publicLookup(); // Just to initialize class
        } catch (Throwable exc) {
            throw new InternalError(exc);
        }
    }

    private JVMHelper() {
    }

    public enum ARCH {
        X86("x86"), X86_64("x86-64"), ARM64("arm64"), ARM32("arm32");

        public final String name;

        ARCH(String name) {
            this.name = name;
        }
    }

    public enum OS {
        WINDOWS("windows"), LINUX("linux"), MACOSX("macosx");

        public final String name;

        OS(String name) {
            this.name = name;
        }

        public static OS byName(String name) {
            if (name.startsWith("Windows"))
                return WINDOWS;
            if (name.startsWith("Linux"))
                return LINUX;
            if (name.startsWith("Mac OS X"))
                return MACOSX;
            throw new RuntimeException(String.format("This shit is not yet supported: '%s'", name));
        }
    }

}
