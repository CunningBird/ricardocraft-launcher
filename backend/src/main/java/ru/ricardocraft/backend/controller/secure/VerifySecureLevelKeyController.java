package ru.ricardocraft.backend.controller.secure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.secure.VerifySecureLevelKeyRequest;
import ru.ricardocraft.backend.controller.AbstractController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.controller.ServerWebSocketHandler;

import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Component
public class VerifySecureLevelKeyController extends AbstractController {

    private final ProtectHandler protectHandler;

    @Autowired
    public VerifySecureLevelKeyController(ServerWebSocketHandler handler, ProtectHandler protectHandler) {
        super(VerifySecureLevelKeyRequest.class, handler);
        this.protectHandler = protectHandler;
    }

    @Override
    public VerifySecureLevelKeyResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        VerifySecureLevelKeyRequest response = (VerifySecureLevelKeyRequest) rawResponse;

        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler) || client.trustLevel == null || client.trustLevel.verifySecureKey == null) {
            throw new Exception("This method not allowed");
        }
        try {
            secureProtectHandler.verifySecureLevelKey(response.publicKey, client.trustLevel.verifySecureKey, response.signature);
        } catch (InvalidKeySpecException e) {
            throw new Exception("Invalid public key");
        } catch (SignatureException e) {
            throw new Exception("Invalid signature");
        } catch (SecurityException e) {
            throw new Exception(e.getMessage());
        }
        client.trustLevel.keyChecked = true;
        client.trustLevel.publicKey = response.publicKey;
        try {
            return secureProtectHandler.onSuccessVerify(client);
        } catch (SecurityException e) {
            throw new Exception(e.getMessage());
        }
    }
}
