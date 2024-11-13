package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "launch-server-config")
public class LaunchServerProperties {
    private String projectName;
    private String[] mirrors;
    private String binaryName;
    private Boolean copyBinaries;
    private LauncherEnvironment env;
    private AuthProperties auth;
    private ProtectHandlerProperties protectHandler;
    private ComponentsProperties components;
    private ProfileProviderProperties profileProvider;
    private UpdatesProviderProperties updatesProvider;
    private NettyProperties netty;
    private LauncherProperties launcher;
    private JarSignerProperties sign;
    private OSSLSignCodeProperties osslSignCodeConfig;
    private RemoteControlProperties remoteControlConfig;
    private MirrorProperties mirrorConfig;
    private LaunchServerRuntimeProperties runtimeConfig;
}