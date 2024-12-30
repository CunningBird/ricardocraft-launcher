package ru.ricardocraft.backend.service.controller.secure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.secure.VerifySecureLevelKeyRequest;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.service.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.SecureProtectHandler;

import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Component
@RequiredArgsConstructor
public class VerifySecureLevelKeyService {

    private final ProtectHandler protectHandler;

    public VerifySecureLevelKeyResponse verifySecureLevelKey(VerifySecureLevelKeyRequest request, Client client) throws Exception {
        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler) || client.trustLevel == null || client.trustLevel.verifySecureKey == null) {
            throw new Exception("This method not allowed");
        }
        try {
            secureProtectHandler.verifySecureLevelKey(request.publicKey, client.trustLevel.verifySecureKey, request.signature);
        } catch (InvalidKeySpecException e) {
            throw new Exception("Invalid public key");
        } catch (SignatureException e) {
            throw new Exception("Invalid signature");
        } catch (SecurityException e) {
            throw new Exception(e.getMessage());
        }
        client.trustLevel.keyChecked = true;
        client.trustLevel.publicKey = request.publicKey;
        try {
            return secureProtectHandler.onSuccessVerify(client);
        } catch (SecurityException e) {
            throw new Exception(e.getMessage());
        }
    }
}
