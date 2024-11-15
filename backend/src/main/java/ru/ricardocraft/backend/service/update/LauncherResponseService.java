package ru.ricardocraft.backend.service.update;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.base.events.request.LauncherRequestEvent;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.helper.SecurityHelper;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.service.auth.RestoreResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.socket.response.update.LauncherResponse;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
public class LauncherResponseService extends AbstractResponseService {

    private final LaunchServerConfig config;
    private final JARLauncherBinary launcherBinary;
    private final EXELauncherBinary exeLauncherBinary;
    private final KeyAgreementManager keyAgreementManager;

    @Autowired
    public LauncherResponseService(WebSocketService service,
                                   LaunchServerConfig config,
                                   JARLauncherBinary launcherBinary,
                                   EXELauncherBinary exeLauncherBinary,
                                   KeyAgreementManager keyAgreementManager) {
        super(LauncherResponse.class, service);
        this.config = config;
        this.launcherBinary = launcherBinary;
        this.exeLauncherBinary = exeLauncherBinary;
        this.keyAgreementManager = keyAgreementManager;
    }

    @Override
    public void execute(WebSocketServerResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        LauncherResponse response = (LauncherResponse) rawResponse;

        byte[] bytes;
        if (response.hash != null)
            bytes = Base64.getDecoder().decode(response.hash);
        else
            bytes = response.digest;
        if (response.launcher_type == 1) {
            byte[] hash = launcherBinary.getDigest();
            if (hash == null)
                service.sendObjectAndClose(ctx, new LauncherRequestEvent(true, config.netty.launcherURL));
            if (Arrays.equals(bytes, hash) && checkSecure(response.secureHash, response.secureSalt)) {
                client.checkSign = true;
                sendResult(ctx, new LauncherRequestEvent(false, config.netty.launcherURL, createLauncherExtendedToken(), config.netty.security.launcherTokenExpire * 1000), response.requestUUID);
            } else {
                sendResultAndClose(ctx, new LauncherRequestEvent(true, config.netty.launcherURL, null, 0), response.requestUUID);
            }
        } else if (response.launcher_type == 2) //EXE
        {
            byte[] hash = exeLauncherBinary.getDigest();
            if (hash == null)
                sendResultAndClose(ctx, new LauncherRequestEvent(true, config.netty.launcherEXEURL), response.requestUUID);
            if (Arrays.equals(bytes, hash) && checkSecure(response.secureHash, response.secureSalt)) {
                client.checkSign = true;
                sendResult(ctx, new LauncherRequestEvent(false, config.netty.launcherEXEURL, createLauncherExtendedToken(), config.netty.security.launcherTokenExpire * 1000), response.requestUUID);
            } else {
                sendResultAndClose(ctx, new LauncherRequestEvent(true, config.netty.launcherEXEURL, null, 0), response.requestUUID);
            }
        } else sendError(ctx, "Request launcher type error", response.requestUUID);
    }

    public String createLauncherExtendedToken() {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .claim("checkSign", true)
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(config.netty.security.launcherTokenExpire).toInstant(ZoneOffset.UTC)))
                .signWith(keyAgreementManager.ecdsaPrivateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private boolean checkSecure(String hash, String salt) {
        if (hash == null || salt == null) return false;
        byte[] normal_hash = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
                config.runtimeConfig.clientCheckSecret.concat(".").concat(salt));
        byte[] launcher_hash = Base64.getDecoder().decode(hash);
        return Arrays.equals(normal_hash, launcher_hash);
    }

    public static class LauncherTokenVerifier implements RestoreResponseService.ExtendedTokenProvider {
        private final JwtParser parser;
        private final Logger logger = LogManager.getLogger();

        public LauncherTokenVerifier(KeyAgreementManager keyAgreementManager) {
            parser = Jwts.parser()
                    .verifyWith(keyAgreementManager.ecdsaPublicKey)
                    .requireIssuer("LaunchServer")
                    .build();
        }

        @Override
        public boolean accept(Client client, AuthProviderPair pair, String extendedToken) {
            try {
                var jwt = parser.parseClaimsJws(extendedToken);
                client.checkSign = jwt.getBody().get("checkSign", Boolean.class);
                client.type = AuthResponse.ConnectTypes.CLIENT;
                return true;
            } catch (Exception e) {
                logger.error("JWT check failed", e);
                return false;
            }

        }
    }
}
