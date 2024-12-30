package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.JoinServerRequest;
import ru.ricardocraft.backend.dto.response.auth.JoinServerResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.AuthException;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.JoinServerProtectHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinServerService {

    private final ProtectHandler protectHandler;
    private final AuthService authService;

    public JoinServerResponse joinServer(JoinServerRequest request, Client client) throws Exception {
        if (!protectHandler.allowJoinServer(client)) {
            throw new Exception("Permissions denied");
        }
        if ((request.username == null && request.uuid == null) || request.accessToken == null || request.serverID == null) {
            throw new Exception("Invalid request");
        }
        boolean success;
        try {
            if (protectHandler instanceof JoinServerProtectHandler joinServerProtectHandler) {
                success = joinServerProtectHandler.onJoinServer(request.serverID, request.username, request.uuid, client);
                if (!success) {
                    return new JoinServerResponse(false);
                }
            }
            success = authService.joinServer(client, request.username, request.uuid, request.accessToken, request.serverID);
            if (success) {
                log.debug("joinServer: {} accessToken: {} serverID: {}", request.username, request.accessToken, request.serverID);
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
