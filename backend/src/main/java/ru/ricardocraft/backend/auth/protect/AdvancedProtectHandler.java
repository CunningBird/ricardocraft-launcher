package ru.ricardocraft.backend.auth.protect;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportHardware;
import ru.ricardocraft.backend.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.dto.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.dto.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.service.auth.RestoreService;
import ru.ricardocraft.backend.socket.Client;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class AdvancedProtectHandler extends StdProtectHandler implements SecureProtectHandler, JoinServerProtectHandler {

    private final Logger logger = LogManager.getLogger(AdvancedProtectHandler.class);

    private final HttpServerProperties httpServerProperties;
    private final KeyAgreementManager keyAgreementManager;

    @Autowired
    public AdvancedProtectHandler(LaunchServerProperties properties,
                                  HttpServerProperties httpServerProperties,
                                  KeyAgreementManager keyAgreementManager) {
        super(properties);
        this.httpServerProperties = httpServerProperties;
        this.keyAgreementManager = keyAgreementManager;
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
                logger.warn("AuthCoreProvider not supported hardware. HardwareInfo not checked!");
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
                .signWith(keyAgreementManager.ecdsaPrivateKey)
                .compact();
    }

    public String createPublicKeyToken(String username, byte[] publicKey) {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SECONDS.toMillis(httpServerProperties.getSecurity().getPublicKeyTokenExpire())))
                .claim("publicKey", Base64.getEncoder().encodeToString(publicKey))
                .signWith(keyAgreementManager.ecdsaPrivateKey)
                .compact();
    }

    public static class HardwareInfoTokenVerifier implements RestoreService.ExtendedTokenProvider {

        private transient final Logger logger = LogManager.getLogger(HardwareInfoTokenVerifier.class);

        private final JwtParser parser;

        public HardwareInfoTokenVerifier(KeyAgreementManager keyAgreementManager) {
            this.parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(keyAgreementManager.ecdsaPublicKey)
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
                logger.error("Hardware JWT error", e);
            }

            return false;
        }
    }

    public static class PublicKeyTokenVerifier implements RestoreService.ExtendedTokenProvider {

        private transient final Logger logger = LogManager.getLogger(PublicKeyTokenVerifier.class);

        private final JwtParser parser;

        public PublicKeyTokenVerifier(KeyAgreementManager keyAgreementManager) {
            this.parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(keyAgreementManager.ecdsaPublicKey)
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
                logger.error("Public Key JWT error", e);
            }

            return false;
        }
    }
}
