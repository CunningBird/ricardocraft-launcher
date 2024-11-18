package ru.ricardocraft.backend.base.profiles.optional.actions;

import java.util.Map;

public class OptionalActionFile extends OptionalAction {

    public Map<String, String> files;

    public OptionalActionFile(Map<String, String> files) {
        this.files = files;
    }
}
