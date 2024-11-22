package ru.ricardocraft.backend.properties;

import io.netty.handler.logging.LogLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.ricardocraft.backend.properties.netty.NettyBindAddressProperties;
import ru.ricardocraft.backend.properties.netty.NettyPerformanceProperties;
import ru.ricardocraft.backend.properties.netty.NettySecurityProperties;
import ru.ricardocraft.backend.properties.netty.NettyUpdatesBindProperties;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "netty")
public class NettyProperties {
    private Boolean fileServerEnabled;
    private Boolean ipForwarding;
    private Boolean disableWebApiInterface;
    private Boolean showHiddenFiles;
    private Boolean sendProfileUpdatesEvent;
    private String launcherURL;
    private String downloadURL;
    private String launcherEXEURL;
    private String address;
    private Map<String, NettyUpdatesBindProperties> bindings;
    private NettyPerformanceProperties performance;

    private NettySecurityProperties security;
    private List<NettyBindAddressProperties> binds;
    private LogLevel logLevel;
}
