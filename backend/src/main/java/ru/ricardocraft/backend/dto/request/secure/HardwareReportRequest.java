package ru.ricardocraft.backend.dto.request.secure;

import ru.ricardocraft.backend.dto.request.AbstractRequest;

public class HardwareReportRequest extends AbstractRequest {

    public ru.ricardocraft.backend.dto.request.HardwareReportRequest.HardwareInfo hardware;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
