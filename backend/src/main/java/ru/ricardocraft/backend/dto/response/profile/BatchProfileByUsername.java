package ru.ricardocraft.backend.dto.response.profile;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

public class BatchProfileByUsername extends SimpleResponse {
    public Entry[] list;

    public static class Entry {
        public String username;
        public String client;
    }
}
