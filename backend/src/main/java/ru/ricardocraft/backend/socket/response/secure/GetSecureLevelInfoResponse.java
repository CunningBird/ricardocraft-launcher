package ru.ricardocraft.backend.socket.response.secure;

import ru.ricardocraft.backend.socket.response.SimpleResponse;

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
