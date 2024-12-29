package ru.ricardocraft.backend.controller.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.SecurityReportRequest;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class SecurityReportController extends AbstractController {

    private final ProtectHandler protectHandler;

    @Autowired
    public SecurityReportController(ServerWebSocketHandler handler, ProtectHandler protectHandler) {
        super(SecurityReportRequest.class, handler);
        this.protectHandler = protectHandler;
    }

    @Override
    public SecurityReportResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        SecurityReportRequest response = (SecurityReportRequest) rawResponse;

        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            throw new Exception("Method not allowed");
        } else {
            return secureProtectHandler.onSecurityReport(response, client);
        }
    }
}
