package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.events.request.JoinServerRequestEvent;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;
import ru.ricardocraft.backend.utils.HookException;

import java.util.UUID;

public class JoinServerResponse extends SimpleResponse {
    private transient final Logger logger = LogManager.getLogger();
    public String serverID;
    public String accessToken;
    public String username;
    public UUID uuid;

    @Override
    public String getType() {
        return "joinServer";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (!server.config.protectHandler.allowJoinServer(client)) {
            sendError("Permissions denied");
            return;
        }
        if ((username == null && uuid == null) || accessToken == null || serverID == null) {
            sendError("Invalid request");
            return;
        }
        boolean success;
        try {
            server.authHookManager.joinServerHook.hook(this, client);
            if (server.config.protectHandler instanceof JoinServerProtectHandler joinServerProtectHandler) {
                success = joinServerProtectHandler.onJoinServer(serverID, username, uuid, client);
                if (!success) {
                    sendResult(new JoinServerRequestEvent(false));
                    return;
                }
            }
            success = server.authManager.joinServer(client, username, uuid, accessToken, serverID);
            if (success) {
                logger.debug("joinServer: {} accessToken: {} serverID: {}", username, accessToken, serverID);
            }
        } catch (AuthException | HookException | SecurityException e) {
            sendError(e.getMessage());
            return;
        } catch (Exception e) {
            logger.error("Join Server error", e);
            sendError("Internal authHandler error");
            return;
        }
        sendResult(new JoinServerRequestEvent(success));
    }

}
