package ru.ricardocraft.backend.binary;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
public class JarLauncherInfo {
    private final List<Path> coreLibs = new ArrayList<>();
    private final List<Path> addonLibs = new ArrayList<>();
    private final Map<String, Path> files = new HashMap<>();
}
