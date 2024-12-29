package ru.ricardocraft.backend.service.mirror.newforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForgeProfileLibraryArtifact {
    private String sha1;
    private Long size;
    private String url;
    private String path;
}
