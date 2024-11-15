package ru.ricardocraft.backend.socket.response.profile;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

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
