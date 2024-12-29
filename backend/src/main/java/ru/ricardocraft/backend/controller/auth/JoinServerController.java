package ru.ricardocraft.backend.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.service.auth.AuthException;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.JoinServerRequest;
import ru.ricardocraft.backend.dto.response.auth.JoinServerResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;

@Slf4j
@Component
public class JoinServerController extends AbstractController {

    private final ProtectHandler protectHandler;
    private final AuthService authService;

    @Autowired
    public JoinServerController(ServerWebSocketHandler handler, ProtectHandler protectHandler, AuthService authService) {
        super(JoinServerRequest.class, handler);
        this.protectHandler = protectHandler;
        this.authService = authService;
    }

    @Override
    public JoinServerResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        JoinServerRequest response = (JoinServerRequest) rawResponse;

        if (!protectHandler.allowJoinServer(client)) {
            throw new Exception("Permissions denied");
        }
        if ((response.username == null && response.uuid == null) || response.accessToken == null || response.serverID == null) {
            throw new Exception("Invalid request");
        }
        boolean success;
        try {
            if (protectHandler instanceof JoinServerProtectHandler joinServerProtectHandler) {
                success = joinServerProtectHandler.onJoinServer(response.serverID, response.username, response.uuid, client);
                if (!success) {
                    return new JoinServerResponse(false);
                }
            }
            success = authService.joinServer(client, response.username, response.uuid, response.accessToken, response.serverID);
            if (success) {
                log.debug("joinServer: {} accessToken: {} serverID: {}", response.username, response.accessToken, response.serverID);
            }
        } catch (AuthException | SecurityException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            log.error("Join Server error", e);
            throw new Exception("Internal authHandler error");
        }
        return new JoinServerResponse(success);
    }
}
