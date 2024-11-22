package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BuildCommandProperties {
    private String workdir;
    private List<String> cmd;
    private Boolean ignoreErrorCode;
    private Map<String, String> env;
}
