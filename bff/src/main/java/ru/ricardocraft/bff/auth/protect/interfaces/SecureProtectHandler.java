package ru.ricardocraft.bff.auth.protect.interfaces;

import ru.ricardocraft.bff.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.bff.base.events.request.SecurityReportRequestEvent;
import ru.ricardocraft.bff.base.events.request.VerifySecureLevelKeyRequestEvent;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.secure.SecurityReportResponse;
import ru.ricardocraft.bff.helper.SecurityHelper;

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
