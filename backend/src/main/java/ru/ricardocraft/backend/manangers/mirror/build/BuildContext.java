package ru.ricardocraft.backend.manangers.mirror.build;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BuildContext {

    public Path scriptBuildDir;
    public Path targetClientDir;
    public Map<String, String> variables = new HashMap<>();

    public void update(String projectName) {
        variables.put("scripttmpdir", scriptBuildDir.toString());
        variables.put("clientdir", targetClientDir.toString());
        variables.put("projectname", projectName);
    }

    public String replace(String str) {
        if (str == null) {
            return null;
        }
        for (var e : variables.entrySet()) {
            str = str.replace("%" + e.getKey() + "%", e.getValue());
        }
        return str;
    }

    public Path createNewBuildDir(String scriptName) throws IOException {
        return Files.createTempDirectory(scriptName);
    }
}