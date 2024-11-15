package ru.ricardocraft.backend.socket.response.auth;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.auth.core.interfaces.session.UserSessionSupportKeys;
import ru.ricardocraft.backend.base.events.request.FetchClientProfileKeyRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class FetchClientProfileKeyResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "clientProfileKey";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (!client.isAuth || client.type != AuthResponse.ConnectTypes.CLIENT) {
            sendError("Permissions denied");
            return;
        }
        UserSession session = client.sessionObject;
        UserSessionSupportKeys.ClientProfileKeys keys;
        if (session instanceof UserSessionSupportKeys support) {
            keys = support.getClientProfileKeys();
        } else {
            keys = authManager.createClientProfileKeys(client.uuid);
        }
        sendResult(new FetchClientProfileKeyRequestEvent(keys.publicKey(), keys.privateKey(), keys.signature(), keys.expiresAt(), keys.refreshedAfter()));
    }
}
