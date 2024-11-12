package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import static java.util.concurrent.TimeUnit.HOURS;

@Getter
@Setter
public class NettySecurityProperties {
    private Long hardwareTokenExpire;
    private Long publicKeyTokenExpire;
    private Long launcherTokenExpire;
}
