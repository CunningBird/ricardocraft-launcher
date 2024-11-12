package ru.ricardocraft.backend.auth.protect;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.events.request.GetSecureLevelInfoRequestEvent;
import ru.ricardocraft.backend.base.events.request.HardwareReportRequestEvent;
import ru.ricardocraft.backend.base.events.request.VerifySecureLevelKeyRequestEvent;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.auth.core.interfaces.provider.AuthSupportHardware;
import ru.ricardocraft.backend.auth.protect.interfaces.HardwareProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.JoinServerProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.SecureProtectHandler;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.RestoreResponse;
import ru.ricardocraft.backend.socket.response.secure.HardwareReportResponse;

import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

public class AdvancedProtectHandler extends StdProtectHandler implements SecureProtectHandler, HardwareProtectHandler, JoinServerProtectHandler {
    private transient final Logger logger = LogManager.getLogger();
    public boolean enableHardwareFeature;
    private transient LaunchServerConfig config;
    private transient KeyAgreementManager keyAgreementManager;

    @Override
    public GetSecureLevelInfoRequestEvent onGetSecureLevelInfo(GetSecureLevelInfoRequestEvent event) {
        return event;
    }

    @Override
    public boolean allowGetSecureLevelInfo(Client client) {
        return client.checkSign;
    }

    @Override
    public void onHardwareReport(HardwareReportResponse response, Client client) {
        if (!enableHardwareFeature) {
            response.sendResult(new HardwareReportRequestEvent());
            return;
        }
        if (!client.isAuth || client.trustLevel == null || client.trustLevel.publicKey == null) {
            response.sendError("Access denied");
            return;
        }
        if(client.trustLevel.hardwareInfo != null) {
            response.sendResult(new HardwareReportRequestEvent(createHardwareToken(client.username, client.trustLevel.hardwareInfo), SECONDS.toMillis(config.netty.security.hardwareTokenExpire)));
            return;
        }
        logger.debug("HardwareInfo received");
        {
            var authSupportHardware = client.auth.isSupport(AuthSupportHardware.class);
            if (authSupportHardware != null) {
                UserHardware hardware = authSupportHardware.getHardwareInfoByData(response.hardware);
                if (hardware == null) {
                    hardware = authSupportHardware.createHardwareInfo(response.hardware, client.trustLevel.publicKey);
                } else {
                    authSupportHardware.addPublicKeyToHardwareInfo(hardware, client.trustLevel.publicKey);
                }
                authSupportHardware.connectUserAndHardware(client.sessionObject, hardware);
                if (hardware.isBanned()) {
                    throw new SecurityException("Your hardware banned");
                }
                client.trustLevel.hardwareInfo = hardware;
                response.sendResult(new HardwareReportRequestEvent(createHardwareToken(client.username, hardware), SECONDS.toMillis(config.netty.security.hardwareTokenExpire)));
            } else {
                logger.error("AuthCoreProvider not supported hardware");
                response.sendError("AuthCoreProvider not supported hardware");
            }
        }
    }

    @Override
    public VerifySecureLevelKeyRequestEvent onSuccessVerify(Client client) {
        if (enableHardwareFeature) {
            var authSupportHardware = client.auth.isSupport(AuthSupportHardware.class);
            if (authSupportHardware != null) {
                UserHardware hardware = authSupportHardware.getHardwareInfoByPublicKey(client.trustLevel.publicKey);
                if (hardware == null) //HWID not found?
                    return new VerifySecureLevelKeyRequestEvent(true, false, createPublicKeyToken(client.username, client.trustLevel.publicKey), SECONDS.toMillis(config.netty.security.publicKeyTokenExpire));
                if (hardware.isBanned()) {
                    throw new SecurityException("Your hardware banned");
                }
                client.trustLevel.hardwareInfo = hardware;
                authSupportHardware.connectUserAndHardware(client.sessionObject, hardware);
                return new VerifySecureLevelKeyRequestEvent(false, false, createPublicKeyToken(client.username, client.trustLevel.publicKey), SECONDS.toMillis(config.netty.security.publicKeyTokenExpire));
            } else {
                logger.warn("AuthCoreProvider not supported hardware. HardwareInfo not checked!");
            }
        }
        return new VerifySecureLevelKeyRequestEvent(false, false, createPublicKeyToken(client.username, client.trustLevel.publicKey), SECONDS.toMillis(config.netty.security.publicKeyTokenExpire));
    }

    @Override
    public boolean onJoinServer(String serverID, String username, UUID uuid, Client client) {
        return !enableHardwareFeature || (client.trustLevel != null && client.trustLevel.hardwareInfo != null);
    }

    @Override
    public void init(LaunchServerConfig config, KeyAgreementManager keyAgreementManager) {
        this.config = config;
        this.keyAgreementManager = keyAgreementManager;
    }

    public String createHardwareToken(String username, UserHardware hardware) {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SECONDS.toMillis(config.netty.security.hardwareTokenExpire)))
                .claim("hardware", hardware.getId())
                .signWith(keyAgreementManager.ecdsaPrivateKey)
                .compact();
    }

    public String createPublicKeyToken(String username, byte[] publicKey) {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + SECONDS.toMillis(config.netty.security.publicKeyTokenExpire)))
                .claim("publicKey", Base64.getEncoder().encodeToString(publicKey))
                .signWith(keyAgreementManager.ecdsaPrivateKey)
                .compact();
    }

    public static class HardwareInfoTokenVerifier implements RestoreResponse.ExtendedTokenProvider {
        private transient final Logger logger = LogManager.getLogger();
        private final JwtParser parser;

        public HardwareInfoTokenVerifier(LaunchServer server) {
            this.parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(server.keyAgreementManager.ecdsaPublicKey)
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

    public static class PublicKeyTokenVerifier implements RestoreResponse.ExtendedTokenProvider {
        private transient final Logger logger = LogManager.getLogger();
        private final JwtParser parser;

        public PublicKeyTokenVerifier(LaunchServer server) {
            this.parser = Jwts.parser()
                    .requireIssuer("LaunchServer")
                    .verifyWith(server.keyAgreementManager.ecdsaPublicKey)
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
