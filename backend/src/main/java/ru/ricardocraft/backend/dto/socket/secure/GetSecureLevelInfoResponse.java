package ru.ricardocraft.backend.dto.socket.secure;

import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class GetSecureLevelInfoResponse extends SimpleResponse {

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
