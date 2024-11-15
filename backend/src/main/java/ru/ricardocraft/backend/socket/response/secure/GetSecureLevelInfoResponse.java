package ru.ricardocraft.backend.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetSecureLevelInfoResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getSecureLevelInfo";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (!(config.protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            GetSecureLevelInfoRequestEvent response = new GetSecureLevelInfoRequestEvent(null);
            response.enabled = false;
            sendResult(response);
            return;
        }
        if (!secureProtectHandler.allowGetSecureLevelInfo(client)) {
            sendError("Access denied");
            return;
        }
        if (client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
        if (client.trustLevel.verifySecureKey == null)
            client.trustLevel.verifySecureKey = secureProtectHandler.generateSecureLevelKey();
        GetSecureLevelInfoRequestEvent response = new GetSecureLevelInfoRequestEvent(client.trustLevel.verifySecureKey);
        response.enabled = true;
        sendResult(secureProtectHandler.onGetSecureLevelInfo(response));
    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
