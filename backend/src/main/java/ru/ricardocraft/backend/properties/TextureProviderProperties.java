package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextureProviderProperties {
    private String skinURL;
    private String cloakURL;
    private TextureProviderType type;
}
