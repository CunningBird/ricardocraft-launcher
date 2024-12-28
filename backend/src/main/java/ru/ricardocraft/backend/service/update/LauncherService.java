package ru.ricardocraft.backend.service.update;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.dto.request.AbstractRequest;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.update.LauncherRequest;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.service.AbstractService;
import ru.ricardocraft.backend.service.auth.RestoreService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
public class LauncherService extends AbstractService {

    private final LaunchServerProperties config;
    private final HttpServerProperties httpServerProperties;
    private final KeyAgreementManager keyAgreementManager;

    @Autowired
    public LauncherService(ServerWebSocketHandler handler,
                           LaunchServerProperties config,
                           HttpServerProperties httpServerProperties,
                           KeyAgreementManager keyAgreementManager) {
        super(LauncherRequest.class, handler);
        this.config = config;
        this.httpServerProperties = httpServerProperties;
        this.keyAgreementManager = keyAgreementManager;
    }

    @Override
    public LauncherResponse execute(AbstractRequest rawResponse, WebSocketSession session, Client client) throws Exception {
        LauncherRequest response = (LauncherRequest) rawResponse;

        byte[] bytes;
        if (response.hash != null)
            bytes = Base64.getDecoder().decode(response.hash);
        else
            bytes = new byte[]{};

        if (response.launcher_type == 1) {
            byte[] hash = getLauncherJarHash(bytes);
            if (hash == null) {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherURL());
                res.closeChannel = true;
                return res;
            }
            if (Arrays.equals(bytes, hash) && checkSecure(response.secureHash, response.secureSalt)) {
                client.checkSign = true;
                return new LauncherResponse(false, httpServerProperties.getLauncherURL(), createLauncherExtendedToken(), httpServerProperties.getSecurity().getLauncherTokenExpire() * 1000);
            } else {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherURL(), null, 0);
                res.closeChannel = true;
                return res;
            }
        } else if (response.launcher_type == 2) { // EXE
            byte[] hash = getLauncherExeHash(bytes);
            if (hash == null) {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherEXEURL());
                res.closeChannel = true;
                return res;
            }
            if (Arrays.equals(bytes, hash) && checkSecure(response.secureHash, response.secureSalt)) {
                client.checkSign = true;
                return new LauncherResponse(false, httpServerProperties.getLauncherEXEURL(), createLauncherExtendedToken(), httpServerProperties.getSecurity().getLauncherTokenExpire() * 1000);
            } else {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherEXEURL(), null, 0);
                res.closeChannel = true;
                return res;
            }
        } else {
            throw new Exception("Request launcher type error");
        }
    }

    public byte[] getLauncherJarHash(byte[] bytes) {
        return bytes; // TODO develop this launcherBinary.getDigest();
    }

    public byte[] getLauncherExeHash(byte[] bytes) {
        return bytes; // TODO develop this exeLauncherBinary.getDigest();
    }

    public String createLauncherExtendedToken() {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .claim("checkSign", true)
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(httpServerProperties.getSecurity().getLauncherTokenExpire()).toInstant(ZoneOffset.UTC)))
                .signWith(keyAgreementManager.ecdsaPrivateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private boolean checkSecure(String hash, String salt) {
        return true;
        // TODO enable this
//        if (hash == null || salt == null) return false;
//        byte[] normal_hash = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
//                config.getRuntime().getClientCheckSecret().concat(".").concat(salt));
//        byte[] launcher_hash = Base64.getDecoder().decode(hash);
//        return Arrays.equals(normal_hash, launcher_hash);
    }

    public static class LauncherTokenVerifier implements RestoreService.ExtendedTokenProvider {

        private final Logger logger = LogManager.getLogger(LauncherTokenVerifier.class);

        private final JwtParser parser;

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
                client.type = AuthRequest.ConnectTypes.CLIENT;
                return true;
            } catch (Exception e) {
                logger.error("JWT multiModCheck failed", e);
                return false;
            }

        }
    }
}
