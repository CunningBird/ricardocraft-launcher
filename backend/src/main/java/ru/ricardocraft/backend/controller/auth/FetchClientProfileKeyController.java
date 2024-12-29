package ru.ricardocraft.backend.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.core.UserSession;
import ru.ricardocraft.backend.service.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.dto.response.auth.FetchClientProfileKeyResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.FetchClientProfileKeyRequest;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

@Component
public class FetchClientProfileKeyController extends AbstractController {

    private final AuthService authService;

    @Autowired
    public FetchClientProfileKeyController(ServerWebSocketHandler handler, AuthService authService) {
        super(FetchClientProfileKeyRequest.class, handler);
        this.authService = authService;
    }

    @Override
    public FetchClientProfileKeyResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        if (!client.isAuth || client.type != AuthRequest.ConnectTypes.CLIENT) {
            throw new Exception("Permissions denied");
        }
        UserSession userSession = client.sessionObject;
        UserSessionSupportKeys.ClientProfileKeys keys;
        if (userSession instanceof UserSessionSupportKeys support) {
            keys = support.getClientProfileKeys();
        } else {
            keys = authService.createClientProfileKeys(client.uuid);
        }
        return new FetchClientProfileKeyResponse(keys.publicKey(), keys.privateKey(), keys.signature(), keys.expiresAt(), keys.refreshedAfter());
    }
}
