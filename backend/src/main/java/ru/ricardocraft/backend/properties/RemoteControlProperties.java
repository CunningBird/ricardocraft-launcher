package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RemoteControlProperties {
    private Boolean enabled;
    private Map<String, RemoteControlTokenProperties> list;
}
