package ru.ricardocraft.backend.dto.profile;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class BatchProfileByUsername extends SimpleResponse {
    public Entry[] list;

    @Override
    public String getType() {
        return "batchProfileByUsername";
    }

    public static class Entry {
        public String username;
        public String client;
    }
}
