package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MirrorProperties {
    private String curseForgeApiKey;
    private Boolean deleteTmpDir;
    private MirrorWorkspaceProperties workspace;
}
