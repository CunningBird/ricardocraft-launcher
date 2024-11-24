package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.ricardocraft.backend.properties.config.*;

@Getter
@Setter
@ConfigurationProperties(prefix = "launch-server-config")
public class LaunchServerProperties {
    private String projectName;
    private String[] mirrors;
    private String binaryName;
    private Boolean copyBinaries;
    private LauncherEnvironment env;

    private TextureProviderProperties textureProvider;
    private AuthLimiterProperties authLimiter;
    private ProguardProperties proguard;
    private LauncherProperties launcher;
    private OpenIDProperties openid;
    private MicrosoftAuthCoreProviderProperties microsoftAuthCoreProvider;
    private JarSignerProperties sign;
    private OSSLSignCodeProperties osslSignCode;
    private RemoteControlProperties remoteControl;
    private MirrorProperties mirror;
    private LaunchServerRuntimeProperties runtime;
    private ProtectHandlerProperties protectHandler;
    private AdvancedProtectHandlerProperties advancedProtectHandler;
    private JsonTextureProviderProperties jsonTextureProvider;

    private LocalUpdatesProviderProperties localUpdatesProvider;
}
