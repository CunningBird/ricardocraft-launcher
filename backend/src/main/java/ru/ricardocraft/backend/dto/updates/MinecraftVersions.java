package ru.ricardocraft.backend.dto.updates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinecraftVersions {
    private List<MiniVersionInfo> versions;
}
