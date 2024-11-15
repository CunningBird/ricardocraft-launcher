package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NettySecurityProperties {
    private Long hardwareTokenExpire;
    private Long publicKeyTokenExpire;
    private Long launcherTokenExpire;
}
