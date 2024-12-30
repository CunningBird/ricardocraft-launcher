package ru.ricardocraft.backend.service.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.response.auth.FetchClientProfileKeyResponse;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.auth.core.UserSession;
import ru.ricardocraft.backend.service.auth.core.interfaces.session.UserSessionSupportKeys;

@Component
@RequiredArgsConstructor
public class FetchClientProfileKeyService {

    private final AuthService authService;

    public FetchClientProfileKeyResponse fetchClientProfileKey(Client client) throws Exception {
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
