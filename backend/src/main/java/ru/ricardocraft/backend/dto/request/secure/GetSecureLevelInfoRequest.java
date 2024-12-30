package ru.ricardocraft.backend.dto.request.secure;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class GetSecureLevelInfoRequest extends AbstractRequest {

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
