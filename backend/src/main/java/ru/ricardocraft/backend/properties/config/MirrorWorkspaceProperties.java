package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MirrorWorkspaceProperties {
    private List<String> fabricMods;
    private List<String> quiltMods;
    private List<String> forgeMods;
    private String lwjgl3version;
    private List<LwjglVersionsProperties> lwjglVersionOverride;
    private String fabricLoaderVersion;
    private Map<String, MultiModProperties> multiMods;
    private List<MirrorLibraryProperties> libraries;
    private Map<String, BuildScriptProperties> build;
}
