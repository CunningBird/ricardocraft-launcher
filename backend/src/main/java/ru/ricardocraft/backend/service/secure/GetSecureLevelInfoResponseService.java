package ru.ricardocraft.backend.service.secure;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.secure.GetSecureLevelInfoResponse;

@Component
public class GetSecureLevelInfoResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    @Autowired
    public GetSecureLevelInfoResponseService(WebSocketService service, ProtectHandler protectHandler) {
        super(GetSecureLevelInfoResponse.class, service);
        this.protectHandler = protectHandler;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        GetSecureLevelInfoResponse response = (GetSecureLevelInfoResponse) rawResponse;

        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            GetSecureLevelInfoRequestEvent res = new GetSecureLevelInfoRequestEvent(null);
            res.enabled = false;
            sendResult(ctx, res, response.requestUUID);
            return;
        }
        if (!secureProtectHandler.allowGetSecureLevelInfo(client)) {
            sendError(ctx, "Access denied", response.requestUUID);
            return;
        }
        if (client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
        if (client.trustLevel.verifySecureKey == null)
            client.trustLevel.verifySecureKey = secureProtectHandler.generateSecureLevelKey();
        GetSecureLevelInfoRequestEvent res = new GetSecureLevelInfoRequestEvent(client.trustLevel.verifySecureKey);
        res.enabled = true;
        sendResult(ctx, secureProtectHandler.onGetSecureLevelInfo(res), response.requestUUID);
    }
}
