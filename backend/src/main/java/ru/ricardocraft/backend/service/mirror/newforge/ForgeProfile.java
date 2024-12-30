package ru.ricardocraft.backend.service.mirror.newforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForgeProfile {
    private String mainClass;
    private String minecraftArguments;
    private ForgeProfileArguments arguments;
    private List<ForgeProfileLibrary> libraries;
}
