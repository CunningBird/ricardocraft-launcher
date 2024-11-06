package ru.ricardocraft.backend.properties;

import ru.ricardocraft.backend.mirror.MirrorWorkspace;

public class MirrorConfig {
    public String curseforgeApiKey = "API_KEY";
    public String workspaceFile;
    public boolean deleteTmpDir;
    public transient MirrorWorkspace workspace;
}