package ru.ricardocraft.client.core;

import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.core.managers.GsonManager;
import ru.ricardocraft.client.core.serialize.HInput;
import ru.ricardocraft.client.launch.DebugLauncherTrustManager;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class Launcher {

    public static final String RUNTIME_DIR = "runtime";
    public static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);

    // Constants
    public static final String CONFIG_FILE = "config.bin";
    private static final AtomicReference<LauncherConfig> CONFIG = new AtomicReference<>();
    public static ClientProfile profile;
    public static GsonManager gsonManager;

    public static String webSocketURL = System.getProperty("launcherdebug.websocket", "ws://localhost:9274/api");
    public static String projectName = System.getProperty("launcherdebug.projectname", "Ricardocraft");
    public static LauncherConfig.LauncherEnvironment environment = LauncherConfig.LauncherEnvironment.valueOf(System.getProperty("launcherdebug.env", "DEBUG"));

    public static LauncherConfig getConfig() {
        LauncherConfig config = CONFIG.get();
        if (config == null) {
            try (HInput input = new HInput(IOHelper.newInput(IOHelper.getResourceURL(CONFIG_FILE)))) {
                config = new LauncherConfig(input);
            } catch (IOException | InvalidKeySpecException e) {
//                throw new SecurityException(e);
                config = new LauncherConfig(webSocketURL, new HashMap<>(), projectName, environment, new DebugLauncherTrustManager(DebugLauncherTrustManager.TrustDebugMode.TRUST_ALL));
            }
            CONFIG.set(config);
        }
        return config;
    }


    public static void setConfig(LauncherConfig cfg) {
        CONFIG.set(cfg);
    }


    public static URL getResourceURL(String name) throws IOException {
        LauncherConfig config = getConfig();
        byte[] validDigest = config.runtime.get(name);
        if (validDigest == null)
            throw new NoSuchFileException(name);

        // Resolve URL and verify digest
        URL url = IOHelper.getResourceURL(RUNTIME_DIR + '/' + name);

        // Return verified URL
        return url;
    }


    public static void applyLauncherEnv(LauncherConfig.LauncherEnvironment env) {
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
