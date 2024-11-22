package ru.ricardocraft.backend.dto.socket.auth;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class SetProfileResponse extends SimpleResponse {
    public String client;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
