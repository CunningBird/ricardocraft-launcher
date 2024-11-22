package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@Getter
@Setter
@ConfigurationProperties(prefix = "directories")
public class DirectoriesProperties {
    private String launcherConfigFile;
    private String cacheFile;
    private Path root;
}
