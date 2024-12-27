package ru.ricardocraft.backend.service.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.events.request.secure.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class GetSecureLevelInfoResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    @Autowired
    public GetSecureLevelInfoResponseService(ServerWebSocketHandler handler, ProtectHandler protectHandler) {
        super(GetSecureLevelInfoResponse.class, handler);
        this.protectHandler = protectHandler;
    }

    @Override
    public GetSecureLevelInfoRequestEvent execute(SimpleResponse rawResponse, WebSocketSession session, Client client) throws Exception {
        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            GetSecureLevelInfoRequestEvent res = new GetSecureLevelInfoRequestEvent(null);
            res.enabled = false;
            return res;
        }
        if (!secureProtectHandler.allowGetSecureLevelInfo(client)) {
            throw new Exception("Access denied");
        }
        if (client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
        if (client.trustLevel.verifySecureKey == null)
            client.trustLevel.verifySecureKey = secureProtectHandler.generateSecureLevelKey();
        GetSecureLevelInfoRequestEvent res = new GetSecureLevelInfoRequestEvent(client.trustLevel.verifySecureKey);
        res.enabled = true;
        return secureProtectHandler.onGetSecureLevelInfo(res);
    }
}
