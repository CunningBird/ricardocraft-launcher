package ru.ricardocraft.backend.properties.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RemoteControlTokenProperties {
    private String token;
    private Long permissions;
    private Boolean allowAll;
    private Boolean startWithMode;
    private List<String> commands;
}
