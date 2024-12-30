package ru.ricardocraft.backend.controller.secure;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.SecurityReportRequest;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.service.controller.secure.SecurityReportService;

@Component
public class SecurityReportController extends AbstractController {

    private final SecurityReportService securityReportService;

    public SecurityReportController(ServerWebSocketHandler handler, SecurityReportService securityReportService) {
        super(SecurityReportRequest.class, handler);
        this.securityReportService = securityReportService;
    }

    @Override
    public SecurityReportResponse execute(AbstractRequest request, WebSocketSession session, Client client) throws Exception {
        return securityReportService.securityReport(castRequest(request), client);
    }
}
