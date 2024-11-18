package ru.ricardocraft.backend.base.profiles.optional.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class OptionalActionFile extends OptionalAction {

    private final Logger logger = LoggerFactory.getLogger(OptionalActionFile.class);

    public Map<String, String> files;

    public OptionalActionFile() {
    }

    public OptionalActionFile(Map<String, String> files) {
        this.files = files;
    }
}
