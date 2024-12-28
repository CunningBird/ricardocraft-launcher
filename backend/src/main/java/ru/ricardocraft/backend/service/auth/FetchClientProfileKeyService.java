package ru.ricardocraft.backend.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.dto.response.auth.FetchClientProfileKeyResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.auth.FetchClientProfileKeyRequest;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class FetchClientProfileKeyService extends AbstractService {

    private final AuthManager authManager;

    @Autowired
    public FetchClientProfileKeyService(ServerWebSocketHandler handler, AuthManager authManager) {
        super(FetchClientProfileKeyRequest.class, handler);
        this.authManager = authManager;
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
            keys = authManager.createClientProfileKeys(client.uuid);
        }
        return new FetchClientProfileKeyResponse(keys.publicKey(), keys.privateKey(), keys.signature(), keys.expiresAt(), keys.refreshedAfter());
    }
}
