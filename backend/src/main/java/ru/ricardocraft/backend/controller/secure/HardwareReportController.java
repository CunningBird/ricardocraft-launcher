package ru.ricardocraft.backend.controller.secure;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.HardwareReportRequest;
import ru.ricardocraft.backend.dto.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.service.controller.secure.HardwareReportService;

@Component
public class HardwareReportController extends AbstractController {

    private final HardwareReportService hardwareReportService;

    public HardwareReportController(ServerWebSocketHandler handler, HardwareReportService hardwareReportService) {
        super(HardwareReportRequest.class, handler);
        this.hardwareReportService = hardwareReportService;
    }

    @Override
    public HardwareReportResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return hardwareReportService.hardwareReport(castRequest(request), client);
    }
}
