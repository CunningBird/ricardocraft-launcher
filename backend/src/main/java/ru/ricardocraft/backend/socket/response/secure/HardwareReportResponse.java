package ru.ricardocraft.backend.socket.response.secure;

import ru.ricardocraft.backend.base.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class HardwareReportResponse extends SimpleResponse {

    public HardwareReportRequest.HardwareInfo hardware;

    @Override
    public String getType() {
        return "hardwareReport";
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
