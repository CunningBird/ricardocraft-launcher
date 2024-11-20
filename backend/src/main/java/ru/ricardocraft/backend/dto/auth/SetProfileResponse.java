package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class SetProfileResponse extends SimpleResponse {
    public String client;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
