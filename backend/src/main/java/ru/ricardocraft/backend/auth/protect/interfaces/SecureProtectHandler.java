package ru.ricardocraft.backend.auth.protect.interfaces;

import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.dto.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.dto.request.secure.SecurityReportRequest;
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

    GetSecureLevelInfoResponse onGetSecureLevelInfo(GetSecureLevelInfoResponse event);

    boolean allowGetSecureLevelInfo(Client client);

    default SecurityReportResponse onSecurityReport(SecurityReportRequest report, Client client) {
        return new SecurityReportResponse();
    }

    default VerifySecureLevelKeyResponse onSuccessVerify(Client client) {
        return new VerifySecureLevelKeyResponse();
    }
}
