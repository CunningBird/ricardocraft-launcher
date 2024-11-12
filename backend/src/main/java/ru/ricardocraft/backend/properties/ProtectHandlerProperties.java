package ru.ricardocraft.backend.properties;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProtectHandlerProperties {
    private Map<String, List<String>> profileWhitelist = new HashMap<>();
    private List<String> allowUpdates = new ArrayList<>();
}
