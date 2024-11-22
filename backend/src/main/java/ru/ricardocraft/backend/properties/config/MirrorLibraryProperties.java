package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class MirrorLibraryProperties {
    private String path;
    private String url;
    private String data;
    private Map<String, String> unpack;
    private List<String> prefixFilter;
}
