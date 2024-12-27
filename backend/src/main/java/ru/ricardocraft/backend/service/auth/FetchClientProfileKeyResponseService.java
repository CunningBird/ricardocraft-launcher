package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.dto.events.request.auth.FetchClientProfileKeyRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.FetchClientProfileKeyResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class FetchClientProfileKeyResponseService extends AbstractResponseService {

    private final AuthManager authManager;

    @Autowired
    public FetchClientProfileKeyResponseService(ServerWebSocketHandler handler, AuthManager authManager) {
        super(FetchClientProfileKeyResponse.class, handler);
        this.authManager = authManager;
    }

    @Override
    public FetchClientProfileKeyRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        if (!client.isAuth || client.type != AuthResponse.ConnectTypes.CLIENT) {
            throw new Exception("Permissions denied");
        }
        UserSession userSession = client.sessionObject;
        UserSessionSupportKeys.ClientProfileKeys keys;
        if (userSession instanceof UserSessionSupportKeys support) {
            keys = support.getClientProfileKeys();
        } else {
            keys = authManager.createClientProfileKeys(client.uuid);
        }
        return new FetchClientProfileKeyRequestEvent(keys.publicKey(), keys.privateKey(), keys.signature(), keys.expiresAt(), keys.refreshedAfter());
    }
}
