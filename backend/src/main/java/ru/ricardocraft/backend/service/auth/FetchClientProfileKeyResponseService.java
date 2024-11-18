package ru.ricardocraft.backend.service.auth;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.base.events.request.FetchClientProfileKeyRequestEvent;
import ru.ricardocraft.backend.dto.SimpleResponse;
import ru.ricardocraft.backend.dto.auth.AuthResponse;
import ru.ricardocraft.backend.dto.auth.FetchClientProfileKeyResponse;
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
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        FetchClientProfileKeyResponse response = (FetchClientProfileKeyResponse) rawResponse;

        if (!client.isAuth || client.type != AuthResponse.ConnectTypes.CLIENT) {
            sendError(ctx,"Permissions denied", response.requestUUID);
            return;
        }
        UserSession session = client.sessionObject;
        UserSessionSupportKeys.ClientProfileKeys keys;
        if (session instanceof UserSessionSupportKeys support) {
            keys = support.getClientProfileKeys();
        } else {
            keys = authManager.createClientProfileKeys(client.uuid);
        }
        sendResult(ctx, new FetchClientProfileKeyRequestEvent(keys.publicKey(), keys.privateKey(), keys.signature(), keys.expiresAt(), keys.refreshedAfter()), response.requestUUID);
    }
}
