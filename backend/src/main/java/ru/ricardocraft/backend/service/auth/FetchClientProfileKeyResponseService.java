package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.dto.events.request.auth.FetchClientProfileKeyRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.auth.FetchClientProfileKeyResponse;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

@Component
public class FetchClientProfileKeyResponseService extends AbstractResponseService {

    private final AuthManager authManager;

    @Autowired
    public FetchClientProfileKeyResponseService(WebSocketService service, AuthManager authManager) {
        super(FetchClientProfileKeyResponse.class, service);
        this.authManager = authManager;
    }

    @Override
    public FetchClientProfileKeyRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        FetchClientProfileKeyResponse response = (FetchClientProfileKeyResponse) rawResponse;

        if (!client.isAuth || client.type != AuthResponse.ConnectTypes.CLIENT) {
            throw new Exception("Permissions denied");
        }
        UserSession session = client.sessionObject;
        UserSessionSupportKeys.ClientProfileKeys keys;
        if (session instanceof UserSessionSupportKeys support) {
            keys = support.getClientProfileKeys();
        } else {
            keys = authManager.createClientProfileKeys(client.uuid);
        }
        return new FetchClientProfileKeyRequestEvent(keys.publicKey(), keys.privateKey(), keys.signature(), keys.expiresAt(), keys.refreshedAfter());
    }
}
