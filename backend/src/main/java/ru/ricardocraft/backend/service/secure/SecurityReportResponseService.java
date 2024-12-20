package ru.ricardocraft.backend.service.secure;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.base.events.request.SecurityReportRequestEvent;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;
import ru.ricardocraft.backend.dto.socket.secure.SecurityReportResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class SecurityReportResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    @Autowired
    public SecurityReportResponseService(WebSocketService service, ProtectHandler protectHandler) {
        super(SecurityReportResponse.class, service);
        this.protectHandler = protectHandler;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        SecurityReportResponse response = (SecurityReportResponse) rawResponse;

        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            sendError(ctx, "Method not allowed", response.requestUUID);
        } else {
            SecurityReportRequestEvent event = secureProtectHandler.onSecurityReport(response, client);
            sendResult(ctx, event, response.requestUUID);
        }
    }
}
