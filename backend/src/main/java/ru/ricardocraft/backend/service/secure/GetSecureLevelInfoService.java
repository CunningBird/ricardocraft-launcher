package ru.ricardocraft.backend.service.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.GetSecureLevelInfoRequest;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

@Component
public class GetSecureLevelInfoService extends AbstractService {

    private final ProtectHandler protectHandler;

    @Autowired
    public GetSecureLevelInfoService(ServerWebSocketHandler handler, ProtectHandler protectHandler) {
        super(GetSecureLevelInfoRequest.class, handler);
        this.protectHandler = protectHandler;
    }

    @Override
    public GetSecureLevelInfoResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler)) {
            GetSecureLevelInfoResponse res = new GetSecureLevelInfoResponse(null);
            res.enabled = false;
            return res;
        }
        if (!secureProtectHandler.allowGetSecureLevelInfo(client)) {
            throw new Exception("Access denied");
        }
        if (client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
        if (client.trustLevel.verifySecureKey == null)
            client.trustLevel.verifySecureKey = secureProtectHandler.generateSecureLevelKey();
        GetSecureLevelInfoResponse res = new GetSecureLevelInfoResponse(client.trustLevel.verifySecureKey);
        res.enabled = true;
        return secureProtectHandler.onGetSecureLevelInfo(res);
    }
}
