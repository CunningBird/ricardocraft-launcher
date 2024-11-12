package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RemoteControlTokenProperties {
    public long permissions;
    public boolean allowAll;
    public boolean startWithMode;
    public List<String> commands;
}
