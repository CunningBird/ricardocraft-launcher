package ru.ricardocraft.backend.properties;

import io.netty.channel.epoll.Epoll;
import io.netty.handler.logging.LogLevel;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.MemoryAuthCoreProvider;
import ru.ricardocraft.backend.auth.profiles.LocalProfileProvider;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.StdProtectHandler;
import ru.ricardocraft.backend.auth.texture.RequestTextureProvider;
import ru.ricardocraft.backend.auth.updates.LocalUpdatesProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.components.AuthLimiterComponent;
import ru.ricardocraft.backend.components.Component;
import ru.ricardocraft.backend.components.ProGuardComponent;
import ru.ricardocraft.backend.helper.SecurityHelper;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.ReconfigurableManager;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;

import java.util.*;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

@org.springframework.stereotype.Component
public final class LaunchServerConfig {

    private transient final Logger logger = LogManager.getLogger();

    private final static List<String> oldMirrorList = List.of("https://mirror.gravit.pro/5.2.x/", "https://mirror.gravit.pro/5.3.x/",
            "https://mirror.gravitlauncher.com/5.2.x/", "https://mirror.gravitlauncher.com/5.3.x/", "https://mirror.gravitlauncher.com/5.4.x/",
            "https://mirror.gravitlauncher.com/5.5.x/");

    @Setter
    public String projectName;
    public String[] mirrors;
    public String binaryName;
    public boolean copyBinaries = true;
    public LauncherEnvironment env;

    public Map<String, AuthProviderPair> auth;
    // Handlers & Providers
    public ProtectHandler protectHandler;
    public Map<String, Component> components;
    public ProfileProvider profileProvider = new LocalProfileProvider();
    public UpdatesProvider updatesProvider = new LocalUpdatesProvider();
    public NettyConfig netty;
    public LauncherConf launcher;
    public JarSignerConf sign;
    public OSSLSignCodeConfig osslSignCodeConfig;
    public RemoteControlConfig remoteControlConfig;
    public MirrorConfig mirrorConfig;

    private transient AuthProviderPair authDefault;
    private transient ReconfigurableManager reconfigurableManager;
    private transient MirrorManager mirrorManager;
    private transient AuthManager authManager;
    private transient NettyServerSocketHandler nettyServerSocketHandler;
    private transient KeyAgreementManager keyAgreementManager;

    public LaunchServerConfig() {
        this.projectName = "ricardocraft";
        this.mirrors = new String[]{"https://mirror.gravitlauncher.com/5.6.x/", "https://gravit-launcher-mirror.storage.googleapis.com/"};
        this.env = LauncherEnvironment.STD;
        this.auth = new HashMap<>();
        AuthProviderPair a = new AuthProviderPair(new MemoryAuthCoreProvider(),
                new RequestTextureProvider("http://example.com/skins/%username%.png", "http://example.com/cloaks/%username%.png")
        );
        a.displayName = "Default";
        this.auth.put("std", a);
        this.protectHandler = new StdProtectHandler();
        this.binaryName = "Launcher";

        this.netty = new NettyConfig();
        String address = "localhost";
        this.netty.address = "ws://" + address + "/api";
        this.netty.downloadURL = "http://" + address + "/%dirname%/";
        this.netty.launcherURL = "http://" + address + "/Launcher.jar";
        this.netty.launcherEXEURL = "http://" + address + "/Launcher.exe";

        this.netty.binds = new NettyBindAddress[]{new NettyBindAddress("0.0.0.0", 9274)};
        this.netty.binds[0].port = 9274;

        this.netty.fileServerEnabled = true;

        this.netty.performance = new NettyPerformanceConfig();
        try {
            this.netty.performance.usingEpoll = Epoll.isAvailable();
        } catch (Throwable e) {
            // Epoll class line 51+ catch (Exception) but Error will be thrown by System.load
            this.netty.performance.usingEpoll = false;
        } // such as on ARM
        this.netty.performance.bossThread = 2;
        this.netty.performance.workerThread = 8;
        this.netty.performance.schedulerThread = 2;

        this.launcher = new LauncherConf();
        this.launcher.compress = true;
        this.launcher.deleteTempFiles = true;
        this.launcher.stripLineNumbers = true;
        this.launcher.customJvmOptions.add("-Dfile.encoding=UTF-8");

        this.sign = new JarSignerConf();

        this.osslSignCodeConfig = new OSSLSignCodeConfig();
        this.osslSignCodeConfig.timestampServer = "http://timestamp.sectigo.com";
        this.osslSignCodeConfig.osslsigncodePath = "osslsigncode";
        this.osslSignCodeConfig.customArgs.add("-h");
        this.osslSignCodeConfig.customArgs.add("sha256");

        this.remoteControlConfig = new RemoteControlConfig();
        this.remoteControlConfig.enabled = true;
        this.remoteControlConfig.list = new ArrayList<>();
        this.remoteControlConfig.list.add(new RemoteControlConfig.RemoteControlToken(SecurityHelper.randomStringToken(), 0, true, new String[0]));

        this.mirrorConfig = new MirrorConfig();

        AuthLimiterComponent authLimiterComponent = new AuthLimiterComponent();
        authLimiterComponent.rateLimit = 3;
        authLimiterComponent.rateLimitMillis = SECONDS.toMillis(8);
        authLimiterComponent.message = "Превышен лимит авторизаций";

        ProGuardComponent proGuardComponent = new ProGuardComponent();

        this.components = new HashMap<>();
        this.components.put("authLimiter", authLimiterComponent);
        this.components.put("proguard", proGuardComponent);

        this.verify();
    }

    public void setLaunchServer(LaunchServer server) {
        this.reconfigurableManager = server.reconfigurableManager;
        this.mirrorManager = server.mirrorManager;
        this.authManager = server.authManager;
        this.nettyServerSocketHandler = server.nettyServerSocketHandler;
        this.keyAgreementManager = server.keyAgreementManager;
    }

    public AuthProviderPair getAuthProviderPair(String name) {
        return auth.get(name);
    }

    public AuthProviderPair getAuthProviderPair() {
        if (authDefault != null) return authDefault;
        for (AuthProviderPair pair : auth.values()) {
            if (pair.isDefault) {
                authDefault = pair;
                return pair;
            }
        }
        throw new IllegalStateException("Default AuthProviderPair not found");
    }

    public void verify() {
        if (auth == null || auth.isEmpty()) {
            throw new NullPointerException("AuthProviderPair`s count should be at least one");
        }

        boolean isOneDefault = false;
        for (AuthProviderPair pair : auth.values()) {
            if (pair.isDefault) {
                isOneDefault = true;
                break;
            }
        }
        if (protectHandler == null) {
            throw new NullPointerException("ProtectHandler must not be null");
        }
        if (!isOneDefault) {
            throw new IllegalStateException("No auth pairs declared by default.");
        }
        if (env == null) {
            throw new NullPointerException("Env must not be null");
        }
        if (netty == null) {
            throw new NullPointerException("Netty must not be null");
        }
        // Mirror check
        {
            boolean updateMirror = Boolean.getBoolean("launchserver.config.disableUpdateMirror");
            if (!updateMirror) {
                for (int i = 0; i < mirrors.length; ++i) {
                    if (mirrors[i] != null && oldMirrorList.contains(mirrors[i])) {
                        logger.warn("Replace mirror '{}' to 'https://mirror.gravitlauncher.com/5.6.x/'. If you really need to use original url, use '-Dlaunchserver.config.disableUpdateMirror=true'", mirrors[i]);
                        mirrors[i] = "https://mirror.gravitlauncher.com/5.6.x/";
                    }
                }
            }
        }
    }

    public void init(ReloadType type) {
        Launcher.applyLauncherEnv(env);
        for (Map.Entry<String, AuthProviderPair> provider : auth.entrySet()) {
            provider.getValue().init(authManager, this, nettyServerSocketHandler, keyAgreementManager, provider.getKey());
        }
        if (protectHandler != null) {
            reconfigurableManager.registerObject("protectHandler", protectHandler);
            protectHandler.init(this, keyAgreementManager);
        }
        if (profileProvider != null) {
            reconfigurableManager.registerObject("profileProvider", profileProvider);
            profileProvider.init(protectHandler);
        }
        if (updatesProvider != null) {
            reconfigurableManager.registerObject("updatesProvider", updatesProvider);
        }
        if (components != null) {
            components.forEach((k, v) -> reconfigurableManager.registerObject("component.".concat(k), v));
        }
        if (!type.equals(ReloadType.NO_AUTH)) {
            for (AuthProviderPair pair : auth.values()) {
                reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".core"), pair.core);
                reconfigurableManager.registerObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
            }
        }
        Arrays.stream(mirrors).forEach(mirrorManager::addMirror);
    }

    public void close(ReloadType type) {
        try {
            if (!type.equals(ReloadType.NO_AUTH)) {
                for (AuthProviderPair pair : auth.values()) {
                    reconfigurableManager.unregisterObject("auth.".concat(pair.name).concat(".core"), pair.core);
                    reconfigurableManager.unregisterObject("auth.".concat(pair.name).concat(".texture"), pair.textureProvider);
                    pair.close();
                }
            }
            if (type.equals(ReloadType.FULL)) {
                components.forEach((k, component) -> {
                    reconfigurableManager.unregisterObject("component.".concat(k), component);
                    if (component instanceof AutoCloseable autoCloseable) {
                        try {
                            autoCloseable.close();
                        } catch (Exception e) {
                            logger.error(e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            logger.error(e);
        }
        if (protectHandler != null) {
            reconfigurableManager.unregisterObject("protectHandler", protectHandler);
            protectHandler.close();
        }
        if (profileProvider != null) {
            reconfigurableManager.unregisterObject("profileProvider", profileProvider);
            profileProvider.close();
        }
        if (updatesProvider != null) {
            reconfigurableManager.unregisterObject("updatesProvider", updatesProvider);
            updatesProvider.close();
        }
    }

    public static class JarSignerConf {
        public boolean enabled = true;
        public String keyStore = "keystore";
        public String keyStoreType = "PKCS12";
        public String keyStorePass = "mypass";
        public String keyAlias = "myname";
        public String keyPass = "mypass";
        public String metaInfKeyName = "SIGNUMO.RSA";
        public String metaInfSfName = "SIGNUMO.SF";
        public String signAlgo = "SHA256WITHRSA";
        public boolean checkCertificateExpired = true;
    }

    public static class NettyUpdatesBind {
        public String url;
        public boolean zip;
    }

    public static class LauncherConf {
        public boolean compress;
        public boolean stripLineNumbers;
        public boolean deleteTempFiles;
        public boolean certificatePinning;
        public boolean encryptRuntime;
        public List<String> customJvmOptions = new ArrayList<>();
        public int memoryLimit = 256;
    }

    public static class NettyConfig {
        public boolean fileServerEnabled;
        public boolean ipForwarding;
        public boolean disableWebApiInterface;
        public boolean showHiddenFiles;
        public boolean sendProfileUpdatesEvent = true;
        public String launcherURL;
        public String downloadURL;
        public String launcherEXEURL;
        public String address;
        public Map<String, NettyUpdatesBind> bindings = new HashMap<>();
        public NettyPerformanceConfig performance;

        public NettySecurityConfig security = new NettySecurityConfig();
        public NettyBindAddress[] binds;
        public LogLevel logLevel = LogLevel.DEBUG;
    }

    public static class NettyPerformanceConfig {
        public boolean usingEpoll;
        public int bossThread;
        public int workerThread;
        public int schedulerThread;
        public int maxWebSocketRequestBytes = 1024 * 1024;
        public boolean disableThreadSafeClientObject;
        public NettyExecutorType executorType = NettyExecutorType.VIRTUAL_THREADS;
    }

    public static class NettyBindAddress {
        public String address;
        public int port;

        public NettyBindAddress(String address, int port) {
            this.address = address;
            this.port = port;
        }
    }

    public static class NettySecurityConfig {
        public long hardwareTokenExpire = HOURS.toSeconds(8);
        public long publicKeyTokenExpire = HOURS.toSeconds(8);

        public long launcherTokenExpire = HOURS.toSeconds(8);
    }

    public static class OSSLSignCodeConfig {
        public String timestampServer;
        public String osslsigncodePath;
        public List<String> customArgs = new ArrayList<>();
        public LaunchServerConfig.JarSignerConf customConf;

        public boolean checkSignSize = true;
        public boolean checkCorrectJar = true;
    }

    public static class RemoteControlConfig {
        public List<RemoteControlToken> list = new ArrayList<>();
        public boolean enabled;

        public RemoteControlToken find(String token) {
            for (RemoteControlToken r : list) {
                if (token.equals(r.token)) {
                    return r;
                }
            }
            return null;
        }

        public static class RemoteControlToken {
            public String token;
            public long permissions;
            public boolean allowAll;
            public boolean startWithMode;
            public List<String> commands;

            public RemoteControlToken(String token, long permissions, boolean allowAll, String[] commands) {
                this.token = token;
                this.permissions = permissions;
                this.allowAll = allowAll;
                this.commands = Arrays.asList(commands.clone());
            }
        }
    }
}
