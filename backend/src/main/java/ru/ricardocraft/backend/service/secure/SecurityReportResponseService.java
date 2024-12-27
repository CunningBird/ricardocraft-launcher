package ru.ricardocraft.backend.service.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.events.request.secure.SecurityReportRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

@Component
public class SecurityReportResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    @Autowired
    public SecurityReportResponseService(ServerWebSocketHandler handler, ProtectHandler protectHandler) {
        super(SecurityReportResponse.class, handler);
        this.protectHandler = protectHandler;
    }

    @Override
    public SecurityReportRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        SecurityReportResponse response = (SecurityReportResponse) rawResponse;

        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            throw new Exception("Method not allowed");
        } else {
            return secureProtectHandler.onSecurityReport(response, client);
        }
    }
}
