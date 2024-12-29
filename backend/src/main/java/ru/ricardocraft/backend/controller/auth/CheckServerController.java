package ru.ricardocraft.backend.controller.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;
import ru.ricardocraft.backend.service.auth.AuthException;
import ru.ricardocraft.backend.service.auth.core.interfaces.session.UserSessionSupportHardware;
import ru.ricardocraft.backend.service.auth.core.interfaces.session.UserSessionSupportProperties;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.CheckServerRequest;
import ru.ricardocraft.backend.dto.response.auth.CheckServerResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;

@Slf4j
@Component
public class CheckServerController extends AbstractController {

    private final AuthService authService;

    @Autowired
    public CheckServerController(ServerWebSocketHandler handler, AuthService authService) {
        super(CheckServerRequest.class, handler);
        this.authService = authService;
    }

    @Override
    public CheckServerResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client pClient) throws Exception {
        CheckServerRequest response = (CheckServerRequest) rawResponse;

        if (pClient.permissions == null || !pClient.permissions.hasPerm("launchserver.checkserver")) {
            throw new Exception("Permissions denied");
        }
        CheckServerResponse result = new CheckServerResponse();
        try {
            AuthService.CheckServerReport report = authService.checkServer(pClient, response.username, response.serverID);
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
            log.debug("checkServer: {} uuid: {} serverID: {}", result.playerProfile == null ? null : result.playerProfile.username, result.uuid, response.serverID);
        } catch (AuthException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            log.error("Internal authHandler error", e);
            throw new Exception("Internal authHandler error");
        }
        return result;
    }
}
