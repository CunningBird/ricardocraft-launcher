package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StdProperties {
    private Boolean isDefault = true;
    private AuthCoreProviderType core;
    private TextureProviderProperties textureProvider;
    private String displayName;
    private Boolean visible;
}
