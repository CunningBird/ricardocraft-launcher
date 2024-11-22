package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.base.events.request.JoinServerRequestEvent;
import ru.ricardocraft.backend.dto.socket.SimpleResponse;
import ru.ricardocraft.backend.dto.socket.auth.JoinServerResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class JoinServerResponseService extends AbstractResponseService {

    private transient final Logger logger = LogManager.getLogger(JoinServerResponseService.class);

    private final ProtectHandler protectHandler;
    private final AuthManager authManager;

    @Autowired
    public JoinServerResponseService(WebSocketService service,
                                        ProtectHandler protectHandler,
                                        AuthManager authManager) {
        super(JoinServerResponse.class, service);
        this.protectHandler = protectHandler;
        this.authManager = authManager;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        JoinServerResponse response = (JoinServerResponse) rawResponse;

        if (!protectHandler.allowJoinServer(client)) {
            sendError(ctx, "Permissions denied", response.requestUUID);
            return;
        }
        if ((response.username == null && response.uuid == null) || response.accessToken == null || response.serverID == null) {
            sendError(ctx,"Invalid request", response.requestUUID);
            return;
        }
        boolean success;
        try {
            if (protectHandler instanceof JoinServerProtectHandler joinServerProtectHandler) {
                success = joinServerProtectHandler.onJoinServer(response.serverID, response.username, response.uuid, client);
                if (!success) {
                    sendResult(ctx, new JoinServerRequestEvent(false), response.requestUUID);
                    return;
                }
            }
            success = authManager.joinServer(client, response.username, response.uuid, response.accessToken, response.serverID);
            if (success) {
                logger.debug("joinServer: {} accessToken: {} serverID: {}", response.username, response.accessToken, response.serverID);
            }
        } catch (AuthException | SecurityException e) {
            sendError(ctx, e.getMessage(), response.requestUUID);
            return;
        } catch (Exception e) {
            logger.error("Join Server error", e);
            sendError(ctx,"Internal authHandler error", response.requestUUID);
            return;
        }
        sendResult(ctx, new JoinServerRequestEvent(success), response.requestUUID);
    }
}
