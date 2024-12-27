package ru.ricardocraft.backend.properties.httpserver;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HttpServerSecurityProperties {
    private Long hardwareTokenExpire;
    private Long publicKeyTokenExpire;
    private Long launcherTokenExpire;
}
