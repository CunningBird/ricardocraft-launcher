package ru.ricardocraft.backend.auth.protect.interfaces;

import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.secure.HardwareReportResponse;

public interface HardwareProtectHandler {
    void onHardwareReport(HardwareReportResponse response, Client client);
}
