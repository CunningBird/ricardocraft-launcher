package ru.ricardocraft.backend.manangers;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.base.utils.Version;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@Component
public class FeaturesManager {

    private final Map<String, String> map = new HashMap<>();

    private final LaunchServerConfig config;

    @PostConstruct
    public void init() {
        addFeatureInfo("version", Version.getVersion().getVersionString());
        addFeatureInfo("projectName", config.projectName);
    }

    public void addFeatureInfo(String name, String featureInfo) {
        map.put(name, featureInfo);
    }
}
