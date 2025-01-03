package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class SetProfileRequest extends AbstractRequest {
    public String client;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
