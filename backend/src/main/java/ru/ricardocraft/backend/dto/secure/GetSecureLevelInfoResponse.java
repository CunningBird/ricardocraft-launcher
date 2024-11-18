package ru.ricardocraft.backend.dto.secure;

import ru.ricardocraft.backend.dto.SimpleResponse;

public class GetSecureLevelInfoResponse extends SimpleResponse {

    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
