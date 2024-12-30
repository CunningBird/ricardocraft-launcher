package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.CheckServerRequest;
import ru.ricardocraft.backend.dto.response.auth.CheckServerResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthException;
import ru.ricardocraft.backend.service.auth.core.interfaces.session.UserSessionSupportHardware;
import ru.ricardocraft.backend.service.auth.core.interfaces.session.UserSessionSupportProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class CheckServerService {

    private final AuthService authService;

    public CheckServerResponse checkServer(CheckServerRequest request, Client pClient) throws Exception {
        if (pClient.permissions == null || !pClient.permissions.hasPerm("launchserver.checkserver")) {
            throw new Exception("Permissions denied");
        }
        CheckServerResponse result = new CheckServerResponse();
        try {
            AuthService.CheckServerReport report = authService.checkServer(pClient, request.username, request.serverID);
            if (report == null) {
                throw new Exception("User not verified");
            }
            result.playerProfile = report.playerProfile;
            result.uuid = report.uuid;
            if (pClient.permissions.hasPerm("launchserver.checkserver.extended") && report.session != null) {
                result.sessionId = report.session.getID();
                if (request.needProperties && report.session instanceof UserSessionSupportProperties supportProperties) {
                    result.sessionProperties = supportProperties.getProperties();
                }
                if (request.needHardware && report.session instanceof UserSessionSupportHardware supportHardware) {
                    result.hardwareId = supportHardware.getHardwareId();
                }
            }
            log.debug("checkServer: {} uuid: {} serverID: {}", result.playerProfile == null ? null : result.playerProfile.username, result.uuid, request.serverID);
        } catch (AuthException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            log.error("Internal authHandler error", e);
            throw new Exception("Internal authHandler error");
        }
        return result;
    }
}
