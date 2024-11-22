package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.dto.updates.VersionType;

@Getter
@Setter
public class MultiModProperties {
    private Version minVersion;
    private Version maxVersion;
    private VersionType type;
    private String url;
    private String target;
}
