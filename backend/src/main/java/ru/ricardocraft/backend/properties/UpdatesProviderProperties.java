package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatesProviderProperties {
    private String cacheFile;
    private String updatesDir;
    private Boolean cacheUpdates;
}
