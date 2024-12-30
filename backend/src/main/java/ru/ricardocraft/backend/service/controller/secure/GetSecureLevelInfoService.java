package ru.ricardocraft.backend.service.controller.secure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.SecureProtectHandler;

@Component
@RequiredArgsConstructor
public class GetSecureLevelInfoService {

    private final ProtectHandler protectHandler;

    public GetSecureLevelInfoResponse getSecureLevelInfoResponse(Client client) throws Exception {
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
