package ru.ricardocraft.backend.dto.secure;

import ru.ricardocraft.backend.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.dto.SimpleResponse;

public class HardwareReportResponse extends SimpleResponse {

    public HardwareReportRequest.HardwareInfo hardware;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
