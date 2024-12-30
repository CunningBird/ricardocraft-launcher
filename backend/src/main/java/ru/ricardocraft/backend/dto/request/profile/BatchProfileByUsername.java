package ru.ricardocraft.backend.dto.request.profile;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class BatchProfileByUsername extends AbstractRequest {
    public Entry[] list;

    public static class Entry {
        public String username;
        public String client;
    }
}
