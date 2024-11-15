package ru.ricardocraft.backend.base;

import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.core.managers.GsonManager;
import ru.ricardocraft.backend.core.serialize.HInput;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.LogHelper;
import ru.ricardocraft.backend.properties.LauncherEnvironment;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public final class Launcher {

    public static final String RUNTIME_DIR = "runtime";

    // Constants
    public static final String CONFIG_FILE = "config.bin";
    private static final AtomicReference<LauncherConfig> CONFIG = new AtomicReference<>();
    private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    public static ClientProfile profile;
    public static GsonManager gsonManager;

    public static LauncherConfig getConfig() {
        LauncherConfig config = CONFIG.get();
        if (config == null) {
            try (HInput input = new HInput(IOHelper.newInput(IOHelper.getResourceURL(CONFIG_FILE)))) {
                config = new LauncherConfig(input);
            } catch (IOException | InvalidKeySpecException e) {
                throw new SecurityException(e);
            }
            CONFIG.set(config);
        }
        return config;
    }


    public static void setConfig(LauncherConfig cfg) {
        CONFIG.set(cfg);
    }

    public static String toHash(UUID uuid) {
        return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
    }

    public static void applyLauncherEnv(LauncherEnvironment env) {
        switch (env) {
            case DEV:
                LogHelper.setDevEnabled(true);
                LogHelper.setStacktraceEnabled(true);
                LogHelper.setDebugEnabled(true);
                break;
            case DEBUG:
                LogHelper.setDebugEnabled(true);
                LogHelper.setStacktraceEnabled(true);
                break;
            case STD:
                break;
            case PROD:
                LogHelper.setStacktraceEnabled(false);
                LogHelper.setDebugEnabled(false);
                LogHelper.setDevEnabled(false);
                break;
        }
    }
}
