package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.dto.updates.VersionType;

import java.util.List;

@Getter
@Setter
public class BuildScriptProperties {
    private List<BuildCommandProperties> script;
    private String result;
    private String path;
    private VersionType type;
    private Version minVersion;
    private Version maxVersion;
    private Boolean dynamic;
}
