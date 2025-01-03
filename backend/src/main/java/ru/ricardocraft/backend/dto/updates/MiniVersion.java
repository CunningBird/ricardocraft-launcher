package ru.ricardocraft.backend.dto.updates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MiniVersion {
    private MinecraftAssetIndexInfo assetIndex;
}
