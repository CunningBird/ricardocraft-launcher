package ru.ricardocraft.backend.dto.secure;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class GetSecureLevelInfoResponse extends SimpleResponse {

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
