package ru.ricardocraft.backend.service.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthException;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportHardware;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportProperties;
import ru.ricardocraft.backend.dto.response.auth.CheckServerResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.CheckServerRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class CheckServerService extends AbstractService {

    private transient final Logger logger = LogManager.getLogger(CheckServerService.class);

    private final AuthManager authManager;

    @Autowired
    public CheckServerService(ServerWebSocketHandler handler, AuthManager authManager) {
        super(CheckServerRequest.class, handler);
        this.authManager = authManager;
    }

    @Override
    public CheckServerResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client pClient) throws Exception {
        CheckServerRequest response = (CheckServerRequest) rawResponse;

        if (pClient.permissions == null || !pClient.permissions.hasPerm("launchserver.checkserver")) {
            throw new Exception("Permissions denied");
        }
        CheckServerResponse result = new CheckServerResponse();
        try {
            AuthManager.CheckServerReport report = authManager.checkServer(pClient, response.username, response.serverID);
            if (report == null) {
                throw new Exception("User not verified");
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
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            logger.error("Internal authHandler error", e);
            throw new Exception("Internal authHandler error");
        }
        return result;
    }
}
