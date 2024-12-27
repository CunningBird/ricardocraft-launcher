package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerBindAddressProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerPerformanceProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerSecurityProperties;
import ru.ricardocraft.backend.properties.httpserver.HttpServerUpdatesBindProperties;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "http-server")
public class HttpServerProperties {
    private Boolean fileServerEnabled;
    private Boolean ipForwarding;
    private Boolean disableWebApiInterface;
    private Boolean showHiddenFiles;
    private Boolean sendProfileUpdatesEvent;
    private String launcherURL;
    private String downloadURL;
    private String launcherEXEURL;
    private String address;
    private Map<String, HttpServerUpdatesBindProperties> bindings;
    private HttpServerPerformanceProperties performance;

    private HttpServerSecurityProperties security;
    private List<HttpServerBindAddressProperties> binds;
}
