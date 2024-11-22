package ru.ricardocraft.backend.manangers.mirror.newforge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForgeProfileArguments {
    private List<String> game;
    private List<String> jvm;
}
