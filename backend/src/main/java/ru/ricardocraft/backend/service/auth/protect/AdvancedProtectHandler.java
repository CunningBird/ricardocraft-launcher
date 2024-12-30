package ru.ricardocraft.backend.service.auth.protect;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.service.auth.core.interfaces.provider.AuthSupportHardware;
import ru.ricardocraft.backend.service.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.service.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.service.KeyAgreementService;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.controller.auth.RestoreController;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.service.controller.auth.RestoreRequestService;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
public class AdvancedProtectHandler extends StdProtectHandler implements SecureProtectHandler, JoinServerProtectHandler {

    private final HttpServerProperties httpServerProperties;
    private final KeyAgreementService keyAgreementService;

    @Autowired
    public AdvancedProtectHandler(LaunchServerProperties properties,
                                  HttpServerProperties httpServerProperties,
                                  KeyAgreementService keyAgreementService) {
        super(properties);
        this.httpServerProperties = httpServerProperties;
        this.keyAgreementService = keyAgreementService;
    }

    @Override
    public GetSecureLevelInfoResponse onGetSecureLevelInfo(GetSecureLevelInfoResponse event) {
        return event;
    }

    @Override
    public boolean allowGetSecureLevelInfo(Client client) {
        return client.checkSign;
    }

    @Override
    public VerifySecureLevelKeyResponse onSuccessVerify(Client client) {
        if (config.getAdvancedProtectHandler().getEnableHardwareFeature()) {
            var authSupportHardware = client.auth.isSupport(AuthSupportHardware.class);
            if (authSupportHardware != null) {
                UserHardware hardware = authSupportHardware.getHardwareInfoByPublicKey(client.trustLevel.publicKey);
                if (hardware == null) //HWID not found?
                    return new VerifySecureLevelKeyResponse(true, false, createPublicKeyToken(client.username, client.trustLevel.publicKey), SECONDS.toMillis(httpServerProperties.getSecurity().getHardwareTokenExpire()));
                if (hardware.isBanned()) {
                    throw new SecurityException("Your hardware banned");
                }
                client.trustLevel.hardwareInfo = hardware;
                authSupportHardware.connectUserAndHardware(client.sessionObject, hardware);
                return new VerifySecureLevelKeyResponse(false, false, createPublicKeyToken(client.username, client.trustLevel.publicKey), SECONDS.toMillis(httpServerProperties.getSecurity().getPublicKeyTokenExpire()));
            } else {
                log.warn("AuthCoreProvider not supported hardware. HardwareInfo not checked!");
            }
        }
        return new VerifySecureLevelKeyResponse(false, false, createPublicKeyToken(client.username, client.trustLevel.publicKey), SECONDS.toMillis(httpServerProperties.getSecurity().getPublicKeyTokenExpire()));
    }

    @Override
    public boolean onJoinServer(String serverID, String username, UUID uuid, Client client) {
        return !config.getAdvancedProtectHandler().getEnableHardwareFeature() || (client.trustLevel != null && client.trustLevel.hardwareInfo != null);
    }

    public String createHardwareToken(String username, UserHardware hardware) {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SECONDS.toMillis(httpServerProperties.getSecurity().getHardwareTokenExpire())))
                .claim("hardware", hardware.getId())
                .signWith(keyAgreementService.ecdsaPrivateKey)
                .compact();
    }

    public String createPublicKeyToken(String username, byte[] publicKey) {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SECONDS.toMillis(httpServerProperties.getSecurity().getPublicKeyTokenExpire())))
                .claim("publicKey", Base64.getEncoder().encodeToString(publicKey))
                .signWith(keyAgreementService.ecdsaPrivateKey)
                .compact();
    }

    public static class HardwareInfoTokenVerifier implements RestoreRequestService.ExtendedTokenProvider {

        private final JwtParser parser;

        public HardwareInfoTokenVerifier(KeyAgreementService keyAgreementService) {
            this.parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(keyAgreementService.ecdsaPublicKey)
                    .build();
        }

        @Override
        public boolean accept(Client client, AuthProviderPair pair, String extendedToken) {
            try {
                var parse = parser.parseClaimsJws(extendedToken);
                String hardwareInfoId = parse.getBody().get("hardware", String.class);
                if (hardwareInfoId == null) return false;
                if (client.auth == null) return false;
                var hardwareSupport = client.auth.core.isSupport(AuthSupportHardware.class);
                if (hardwareSupport == null) return false;
                UserHardware hardware = hardwareSupport.getHardwareInfoById(hardwareInfoId);
                if (client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
                client.trustLevel.hardwareInfo = hardware;
                return true;
            } catch (Throwable e) {
                log.error("Hardware JWT error", e);
            }

            return false;
        }
    }

    public static class PublicKeyTokenVerifier implements RestoreRequestService.ExtendedTokenProvider {

        private final JwtParser parser;

        public PublicKeyTokenVerifier(KeyAgreementService keyAgreementService) {
            this.parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(keyAgreementService.ecdsaPublicKey)
                    .build();
        }

        @Override
        public boolean accept(Client client, AuthProviderPair pair, String extendedToken) {
            try {
                var parse = parser.parseClaimsJws(extendedToken);
                String publicKey = parse.getBody().get("publicKey", String.class);
                if (publicKey == null) return false;
                if (client.trustLevel == null) client.trustLevel = new Client.TrustLevel();
                client.trustLevel.publicKey = Base64.getDecoder().decode(publicKey);
                return true;
            } catch (Throwable e) {
                log.error("Public Key JWT error", e);
            }

            return false;
        }
    }
}
