package ru.ricardocraft.backend.auth.protect.interfaces;

import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.dto.events.request.secure.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.backend.dto.events.request.secure.SecurityReportRequestEvent;
import ru.ricardocraft.backend.dto.events.request.secure.VerifySecureLevelKeyRequestEvent;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.socket.Client;

import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

public interface SecureProtectHandler {
    default byte[] generateSecureLevelKey() {
        return SecurityHelper.randomBytes(128);
    }

    default void verifySecureLevelKey(byte[] publicKey, byte[] data, byte[] signature) throws InvalidKeySpecException, SignatureException {
        if (publicKey == null || signature == null) throw new InvalidKeySpecException();
        ECPublicKey pubKey = SecurityHelper.toPublicECDSAKey(publicKey);
        Signature sign = SecurityHelper.newECVerifySignature(pubKey);
        sign.update(data);
        sign.verify(signature);
    }

    GetSecureLevelInfoRequestEvent onGetSecureLevelInfo(GetSecureLevelInfoRequestEvent event);

    boolean allowGetSecureLevelInfo(Client client);

    default SecurityReportRequestEvent onSecurityReport(SecurityReportResponse report, Client client) {
        return new SecurityReportRequestEvent();
    }

    default VerifySecureLevelKeyRequestEvent onSuccessVerify(Client client) {
        return new VerifySecureLevelKeyRequestEvent();
    }
}
