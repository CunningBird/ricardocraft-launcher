package ru.ricardocraft.backend.service.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.dto.events.request.auth.JoinServerRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.JoinServerResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class JoinServerResponseService extends AbstractResponseService {

    private transient final Logger logger = LogManager.getLogger(JoinServerResponseService.class);

    private final ProtectHandler protectHandler;
    private final AuthManager authManager;

    @Autowired
    public JoinServerResponseService(ServerWebSocketHandler handler, ProtectHandler protectHandler, AuthManager authManager) {
        super(JoinServerResponse.class, handler);
        this.protectHandler = protectHandler;
        this.authManager = authManager;
    }

    @Override
    public JoinServerRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        JoinServerResponse response = (JoinServerResponse) rawResponse;

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
                    return new JoinServerRequestEvent(false);
                }
            }
            success = authManager.joinServer(client, response.username, response.uuid, response.accessToken, response.serverID);
            if (success) {
                logger.debug("joinServer: {} accessToken: {} serverID: {}", response.username, response.accessToken, response.serverID);
            }
        } catch (AuthException | SecurityException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            logger.error("Join Server error", e);
            throw new Exception("Internal authHandler error");
        }
        return new JoinServerRequestEvent(success);
    }
}
