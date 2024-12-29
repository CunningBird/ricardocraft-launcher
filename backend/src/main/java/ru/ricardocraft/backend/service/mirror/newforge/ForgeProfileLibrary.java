package ru.ricardocraft.backend.service.mirror.newforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForgeProfileLibrary {
    private String name;
    private ForgeProfileLibraryDownload downloads;
}
