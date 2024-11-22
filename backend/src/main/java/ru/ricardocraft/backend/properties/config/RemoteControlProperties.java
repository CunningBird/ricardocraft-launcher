package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RemoteControlProperties {
    private Boolean enabled;
    private List<RemoteControlTokenProperties> list;
}
