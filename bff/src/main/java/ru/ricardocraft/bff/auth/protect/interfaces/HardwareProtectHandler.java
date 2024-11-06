package ru.ricardocraft.bff.auth.protect.interfaces;

import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.secure.HardwareReportResponse;

public interface HardwareProtectHandler {
    void onHardwareReport(HardwareReportResponse response, Client client);
}
