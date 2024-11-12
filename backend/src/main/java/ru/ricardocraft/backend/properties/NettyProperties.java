package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
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
}
