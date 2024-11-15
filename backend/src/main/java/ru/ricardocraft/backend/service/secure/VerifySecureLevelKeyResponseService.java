package ru.ricardocraft.backend.service.secure;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.secure.VerifySecureLevelKeyResponse;

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
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        VerifySecureLevelKeyResponse response = (VerifySecureLevelKeyResponse) rawResponse;

        if (!(protectHandler instanceof SecureProtectHandler secureProtectHandler) || client.trustLevel == null || client.trustLevel.verifySecureKey == null) {
            sendError(ctx, "This method not allowed", response.requestUUID);
            return;
        }
        try {
            secureProtectHandler.verifySecureLevelKey(response.publicKey, client.trustLevel.verifySecureKey, response.signature);
        } catch (InvalidKeySpecException e) {
            sendError(ctx, "Invalid public key", response.requestUUID);
            return;
        } catch (SignatureException e) {
            sendError(ctx, "Invalid signature", response.requestUUID);
            return;
        } catch (SecurityException e) {
            sendError(ctx, e.getMessage(), response.requestUUID);
            return;
        }
        client.trustLevel.keyChecked = true;
        client.trustLevel.publicKey = response.publicKey;
        try {
            sendResult(ctx, secureProtectHandler.onSuccessVerify(client), response.requestUUID);
        } catch (SecurityException e) {
            sendError(ctx, e.getMessage(), response.requestUUID);
        }
    }
}
