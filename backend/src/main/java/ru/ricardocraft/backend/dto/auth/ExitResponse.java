package ru.ricardocraft.backend.dto.auth;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class ExitResponse extends SimpleResponse {
    public boolean exitAll;
    public String username;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
