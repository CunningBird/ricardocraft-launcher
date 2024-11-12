package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MirrorProperties {
    private String curseForgeApiKey;
    private String workspaceFile;
    private boolean deleteTmpDir;
}
