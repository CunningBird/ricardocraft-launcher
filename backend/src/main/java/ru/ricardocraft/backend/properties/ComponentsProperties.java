package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentsProperties {
    private AuthLimiterProperties authLimiter;
    private ProguardProperties proguard;
}
