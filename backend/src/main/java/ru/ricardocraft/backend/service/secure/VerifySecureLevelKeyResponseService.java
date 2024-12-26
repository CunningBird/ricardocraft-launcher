package ru.ricardocraft.backend.service.secure;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.events.request.secure.VerifySecureLevelKeyRequestEvent;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

@Component
public class VerifySecureLevelKeyResponseService extends AbstractResponseService {

    private final ProtectHandler protectHandler;

    @Autowired
    public VerifySecureLevelKeyResponseService(WebSocketService service, ProtectHandler protectHandler) {
        super(VerifySecureLevelKeyResponse.class, service);
        this.protectHandler = protectHandler;
    }

    @Override
    public VerifySecureLevelKeyRequestEvent execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        VerifySecureLevelKeyResponse response = (VerifySecureLevelKeyResponse) rawResponse;

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
