package ru.ricardocraft.backend.dto.socket.secure;

import ru.ricardocraft.backend.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;

public class HardwareReportResponse extends SimpleResponse {

    public HardwareReportRequest.HardwareInfo hardware;

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
