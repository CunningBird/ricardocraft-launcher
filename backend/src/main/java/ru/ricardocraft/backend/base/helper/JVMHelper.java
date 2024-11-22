package ru.ricardocraft.backend.base.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Locale;

public final class JVMHelper {

    private static final Logger logger = LoggerFactory.getLogger(JVMHelper.class);

    // MXBeans exports
    public static final RuntimeMXBean RUNTIME_MXBEAN = ManagementFactory.getRuntimeMXBean();
    public static final OperatingSystemMXBean OPERATING_SYSTEM_MXBEAN = ManagementFactory.getOperatingSystemMXBean();
    public static final OS OS_TYPE = OS.byName(OPERATING_SYSTEM_MXBEAN.getName());
    public static final Runtime RUNTIME = Runtime.getRuntime();
    public static final ClassLoader LOADER = ClassLoader.getSystemClassLoader();

    static {
        try {
            MethodHandles.publicLookup(); // Just to initialize class
        } catch (Throwable exc) {
            throw new InternalError(exc);
        }
    }

    private JVMHelper() {
    }

    public static int getVersion() {
        //System.out.println("[DEBUG] JVMHelper 11 version");
        return Runtime.version().feature();
    }

    public static int getBuild() {
        return Runtime.version().update();
    }

    public static void fullGC() {
        RUNTIME.gc();
        logger.debug("Used heap: {} MiB", RUNTIME.totalMemory() - RUNTIME.freeMemory() >> 20);
    }

    public static X509Certificate[] getCertificates(Class<?> clazz) {
        Object[] signers = clazz.getSigners();
        if (signers == null) return null;
        return Arrays.stream(signers).filter((c) -> c instanceof X509Certificate).map((c) -> (X509Certificate) c).toArray(X509Certificate[]::new);
    }

    public static void verifySystemProperties(Class<?> mainClass, boolean requireSystem) {
        Locale.setDefault(Locale.US);
        // Verify class loader
        logger.debug("Verifying class loader");
        if (requireSystem && !mainClass.getClassLoader().equals(LOADER))
            throw new SecurityException("ClassLoader should be system");

        // Verify system and java architecture
        logger.debug("Verifying JVM architecture");
    }

    public enum ARCH {
        X86("x86"), X86_64("x86-64"), ARM64("arm64"), ARM32("arm32");

        public final String name;

        ARCH(String name) {
            this.name = name;
        }
    }

    public enum OS {
        MUSTDIE("mustdie"), LINUX("linux"), MACOSX("macosx");

        public final String name;

        OS(String name) {
            this.name = name;
        }

        public static OS byName(String name) {
            if (name.startsWith("Windows"))
                return MUSTDIE;
            if (name.startsWith("Linux"))
                return LINUX;
            if (name.startsWith("Mac OS X"))
                return MACOSX;
            throw new RuntimeException(String.format("This shit is not yet supported: '%s'", name));
        }
    }

}
