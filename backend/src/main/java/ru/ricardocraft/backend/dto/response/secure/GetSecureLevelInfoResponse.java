package ru.ricardocraft.backend.dto.response.secure;

import ru.ricardocraft.backend.dto.response.SimpleResponse;

public class GetSecureLevelInfoResponse extends SimpleResponse {

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
