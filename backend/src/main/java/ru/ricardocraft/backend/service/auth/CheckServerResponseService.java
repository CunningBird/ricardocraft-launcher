package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportHardware;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportProperties;
import ru.ricardocraft.backend.base.events.request.CheckServerRequestEvent;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.auth.CheckServerResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class CheckServerResponseService extends AbstractResponseService {

    private transient final Logger logger = LogManager.getLogger(CheckServerResponseService.class);

    private final AuthManager authManager;

    @Autowired
    public CheckServerResponseService(WebSocketService service,
                                      AuthManager authManager) {
        super(CheckServerResponse.class, service);
        this.authManager = authManager;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client pClient) throws Exception {
        CheckServerResponse response = (CheckServerResponse) rawResponse;

        if (pClient.permissions == null || !pClient.permissions.hasPerm("launchserver.checkserver")) {
            sendError(ctx, "Permissions denied", response.requestUUID);
            return;
        }
        CheckServerRequestEvent result = new CheckServerRequestEvent();
        try {
            AuthManager.CheckServerReport report = authManager.checkServer(pClient, response.username, response.serverID);
            if (report == null) {
                sendError(ctx, "User not verified", response.requestUUID);
                return;
            }
            result.playerProfile = report.playerProfile;
            result.uuid = report.uuid;
            if (pClient.permissions.hasPerm("launchserver.checkserver.extended") && report.session != null) {
                result.sessionId = report.session.getID();
                if (response.needProperties && report.session instanceof UserSessionSupportProperties supportProperties) {
                    result.sessionProperties = supportProperties.getProperties();
                }
                if (response.needHardware && report.session instanceof UserSessionSupportHardware supportHardware) {
                    result.hardwareId = supportHardware.getHardwareId();
                }
            }
            logger.debug("checkServer: {} uuid: {} serverID: {}", result.playerProfile == null ? null : result.playerProfile.username, result.uuid, response.serverID);
        } catch (AuthException e) {
            sendError(ctx, e.getMessage(), response.requestUUID);
            return;
        } catch (Exception e) {
            logger.error("Internal authHandler error", e);
            sendError(ctx, "Internal authHandler error", response.requestUUID);
            return;
        }
        sendResult(ctx, result, response.requestUUID);
    }
}
