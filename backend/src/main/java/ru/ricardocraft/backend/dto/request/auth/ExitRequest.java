package ru.ricardocraft.backend.dto.request.auth;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class ExitRequest extends AbstractRequest {
    public boolean exitAll;
    public String username;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
