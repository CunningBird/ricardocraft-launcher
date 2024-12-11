package ru.ricardocraft.client.base;

import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.core.managers.GsonManager;
import ru.ricardocraft.client.core.serialize.HInput;
import ru.ricardocraft.client.launch.DebugLauncherTrustManager;
import ru.ricardocraft.client.utils.helper.IOHelper;
import ru.ricardocraft.client.utils.helper.JVMHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public final class Launcher {

    // Used to determine from clientside is launched from launcher
    public static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);
    public static final String RUNTIME_DIR = "runtime";

    // Constants
    public static final String CONFIG_FILE = "config.bin";
    private static final AtomicReference<LauncherConfig> CONFIG = new AtomicReference<>();
    private static final Pattern UUID_PATTERN = Pattern.compile("-", Pattern.LITERAL);
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


    public static String toHash(UUID uuid) {
        return UUID_PATTERN.matcher(uuid.toString()).replaceAll("");
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

    public static String makeSpecialGuardDirName(JVMHelper.ARCH arch, JVMHelper.OS os) {
        return String.format("%s-%s", arch.name, os.name);
    }
}
