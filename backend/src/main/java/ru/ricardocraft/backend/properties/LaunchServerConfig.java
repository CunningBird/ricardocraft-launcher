package ru.ricardocraft.backend.properties;

import io.netty.channel.epoll.Epoll;
import io.netty.handler.logging.LogLevel;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.SecurityHelper;

import java.net.URI;
import java.util.*;

import static java.util.concurrent.TimeUnit.HOURS;

@Component
public final class LaunchServerConfig {

    @Setter
    public String projectName;
    public String[] mirrors;
    public String binaryName;
    public LauncherEnvironment env;

    public NettyConfig netty;
    public LauncherConf launcher;
    public JarSignerConf sign;
    public RemoteControlConfig remoteControlConfig;
    public MirrorConfig mirrorConfig;
    public RuntimeConfig runtimeConfig;
    public OpenIDConfig openIDConfig;
    public LocalUpdatesProviderConfig localUpdatesProviderConfig;
    public LocalProfileProvider localProfileProvider;
    public JsonTextureProviderConfig jsonTextureProviderConfig;
    public AdvancedProtectHandlerConfig advancedProtectHandlerConfig;
    public StdProtectHandlerConfig stdProtectHandlerConfig;
    public MicrosoftAuthCoreProviderConfig microsoftAuthCoreProviderConfig;

    public LaunchServerConfig() {
        this.projectName = "ricardocraft";
        this.mirrors = new String[]{"https://mirror.gravitlauncher.com/5.6.x/", "https://gravit-launcher-mirror.storage.googleapis.com/"};
        this.env = LauncherEnvironment.STD;
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
        this.sign.osslSignCodeConfig = new OSSLSignCodeConfig();
        this.sign.osslSignCodeConfig.timestampServer = "http://timestamp.sectigo.com";
        this.sign.osslSignCodeConfig.osslsigncodePath = "osslsigncode";
        this.sign.osslSignCodeConfig.customArgs.add("-h");
        this.sign.osslSignCodeConfig.customArgs.add("sha256");

        this.remoteControlConfig = new RemoteControlConfig();
        this.remoteControlConfig.enabled = true;
        this.remoteControlConfig.list = new ArrayList<>();
        this.remoteControlConfig.list.add(new RemoteControlConfig.RemoteControlToken(SecurityHelper.randomStringToken(), 0, true, new String[0]));

        this.mirrorConfig = new MirrorConfig();

        this.openIDConfig = new OpenIDConfig(
                URI.create("http://localhost:8079"),
                "",
                "",
                "",
                "",
                URI.create("http://localhost:8079"),
                "",
                "",
                new OpenIDConfig.ClaimExtractorConfig("", "")
        );

        this.localUpdatesProviderConfig = new LocalUpdatesProviderConfig();
        this.localProfileProvider = new LocalProfileProvider();
        this.jsonTextureProviderConfig = new JsonTextureProviderConfig();
        this.advancedProtectHandlerConfig = new AdvancedProtectHandlerConfig();
        this.stdProtectHandlerConfig = new StdProtectHandlerConfig();
        this.microsoftAuthCoreProviderConfig = new MicrosoftAuthCoreProviderConfig();
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
        public OSSLSignCodeConfig osslSignCodeConfig;
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

    public static class RuntimeConfig {
        public String passwordEncryptKey = SecurityHelper.randomStringToken();
        public String runtimeEncryptKey = SecurityHelper.randomStringAESKey();
        public String unlockSecret;
        public String registerApiKey = SecurityHelper.randomStringToken();
        public String clientCheckSecret = SecurityHelper.randomStringToken();
        public long buildNumber = 0;
    }

    public record OpenIDConfig(URI tokenUri,
                               String authorizationEndpoint,
                               String clientId,
                               String clientSecret,
                               String redirectUri,
                               URI jwksUri,
                               String scopes,
                               String issuer,
                               ClaimExtractorConfig extractorConfig) {

        public record ClaimExtractorConfig(String usernameClaim, String uuidClaim) {
        }
    }

    public static class LocalUpdatesProviderConfig {
        public String cacheFile = ".updates-cache";
        public String updatesDir = "updates";
        public boolean cacheUpdates = true;
    }

    public static class LocalProfileProvider {
        public String profilesDir = "profiles";
    }

    public static class JsonTextureProviderConfig {
        public String url;
        public String bearerToken;
    }

    public static class AdvancedProtectHandlerConfig {
        public boolean enableHardwareFeature;
    }

    public static class StdProtectHandlerConfig {
        public Map<String, List<String>> profileWhitelist = new HashMap<>();
        public List<String> allowUpdates = new ArrayList<>();
    }

    public static class MicrosoftAuthCoreProviderConfig {
        public String redirectUrl = "https://login.live.com/oauth20_desktop.srf";
        public String clientId = "00000000402b5328";
        public String clientSecret;
    }
}
