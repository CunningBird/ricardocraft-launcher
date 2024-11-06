package ru.ricardocraft.backend.manangers;

import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.utils.Version;

import java.util.HashMap;
import java.util.Map;

public class FeaturesManager {
    private final Map<String, String> map;

    public FeaturesManager(LaunchServer server) {
        map = new HashMap<>();
        addFeatureInfo("version", Version.getVersion().getVersionString());
        addFeatureInfo("projectName", server.config.projectName);
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String getFeatureInfo(String name) {
        return map.get(name);
    }

    public void addFeatureInfo(String name, String featureInfo) {
        map.put(name, featureInfo);
    }

    public String removeFeatureInfo(String name) {
        return map.remove(name);
    }
}
