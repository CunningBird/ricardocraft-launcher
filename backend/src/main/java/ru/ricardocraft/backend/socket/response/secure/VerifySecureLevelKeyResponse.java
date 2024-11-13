package ru.ricardocraft.backend.socket.response.secure;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

public class VerifySecureLevelKeyResponse extends SimpleResponse {
    public byte[] publicKey;
    public byte[] signature;

    @Override
    public String getType() {
        return "verifySecureLevelKey";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        if (!(config.protectHandler instanceof SecureProtectHandler secureProtectHandler) || client.trustLevel == null || client.trustLevel.verifySecureKey == null) {
            sendError("This method not allowed");
            return;
        }
        try {
            secureProtectHandler.verifySecureLevelKey(publicKey, client.trustLevel.verifySecureKey, signature);
        } catch (InvalidKeySpecException e) {
            sendError("Invalid public key");
            return;
        } catch (SignatureException e) {
            sendError("Invalid signature");
            return;
        } catch (SecurityException e) {
            sendError(e.getMessage());
            return;
        }
        client.trustLevel.keyChecked = true;
        client.trustLevel.publicKey = publicKey;
        try {
            sendResult(secureProtectHandler.onSuccessVerify(client));
        } catch (SecurityException e) {
            sendError(e.getMessage());
        }

    }

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }
}
