package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProguardProperties {
    private String modeAfter;
    private String dir;
    private List<String> jvmArgs = new ArrayList<>();
    private Boolean enabled = true;
    private Boolean mappings = true;
}
