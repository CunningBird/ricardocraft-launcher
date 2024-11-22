package ru.ricardocraft.backend.dto.socket.profile;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class BatchProfileByUsername extends SimpleResponse {
    public Entry[] list;

    public static class Entry {
        public String username;
        public String client;
    }
}
