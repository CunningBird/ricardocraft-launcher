package ru.ricardocraft.backend.dto.response.auth;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

public class SetProfileResponse extends SimpleResponse {
    public String client;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
