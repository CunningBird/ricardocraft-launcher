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
import ru.ricardocraft.backend.dto.events.request.update.LauncherRequestEvent;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.dto.response.SimpleResponse;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.service.AbstractResponseService;
import ru.ricardocraft.backend.service.auth.RestoreResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
public class LauncherResponseService extends AbstractResponseService {

    private final LaunchServerProperties config;
    private final NettyProperties nettyProperties;
    private final KeyAgreementManager keyAgreementManager;

    @Autowired
    public LauncherResponseService(WebSocketService service,
                                   LaunchServerProperties config,
                                   NettyProperties nettyProperties,
                                   KeyAgreementManager keyAgreementManager) {
        super(LauncherResponse.class, service);
        this.config = config;
        this.nettyProperties = nettyProperties;
        this.keyAgreementManager = keyAgreementManager;
    }

    @Override
    public void execute(SimpleResponse rawResponse, ChannelHandlerContext ctx, Client client) throws Exception {
        LauncherResponse response = (LauncherResponse) rawResponse;

//        byte[] bytes;
//        if (response.hash != null)
//            bytes = Base64.getDecoder().decode(response.hash);
//        else
//            bytes = response.digest;
        if (response.launcher_type == 1) {
            sendResult(ctx, new LauncherRequestEvent(false, nettyProperties.getLauncherURL(), createLauncherExtendedToken(), nettyProperties.getSecurity().getLauncherTokenExpire() * 1000), response.requestUUID);
//            byte[] hash = launcherBinary.getDigest();
//            if (hash == null)
//                service.sendObjectAndClose(ctx, new LauncherRequestEvent(true, nettyProperties.getLauncherURL()));
//            if (Arrays.equals(bytes, hash) && checkSecure(response.secureHash, response.secureSalt)) {
//                client.checkSign = true;
//                sendResult(ctx, new LauncherRequestEvent(false, nettyProperties.getLauncherURL(), createLauncherExtendedToken(), nettyProperties.getSecurity().getLauncherTokenExpire() * 1000), response.requestUUID);
//            } else {
//                sendResultAndClose(ctx, new LauncherRequestEvent(true, nettyProperties.getLauncherURL(), null, 0), response.requestUUID);
//            }
        } else if (response.launcher_type == 2) //EXE
        {
            sendResult(ctx, new LauncherRequestEvent(false, nettyProperties.getLauncherEXEURL(), createLauncherExtendedToken(), nettyProperties.getSecurity().getLauncherTokenExpire() * 1000), response.requestUUID);
//            byte[] hash = exeLauncherBinary.getDigest();
//            if (hash == null)
//                sendResultAndClose(ctx, new LauncherRequestEvent(true, nettyProperties.getLauncherEXEURL()), response.requestUUID);
//            if (Arrays.equals(bytes, hash) && checkSecure(response.secureHash, response.secureSalt)) {
//                client.checkSign = true;
//                sendResult(ctx, new LauncherRequestEvent(false, nettyProperties.getLauncherEXEURL(), createLauncherExtendedToken(), nettyProperties.getSecurity().getLauncherTokenExpire() * 1000), response.requestUUID);
//            } else {
//                sendResultAndClose(ctx, new LauncherRequestEvent(true, nettyProperties.getLauncherEXEURL(), null, 0), response.requestUUID);
//            }
        } else sendError(ctx, "Request launcher type error", response.requestUUID);
    }

    public String createLauncherExtendedToken() {
        return Jwts.builder()
                .setIssuer("LaunchServer")
                .claim("checkSign", true)
                .setExpiration(Date.from(LocalDateTime.now().plusSeconds(nettyProperties.getSecurity().getLauncherTokenExpire()).toInstant(ZoneOffset.UTC)))
                .signWith(keyAgreementManager.ecdsaPrivateKey, SignatureAlgorithm.ES256)
                .compact();
    }

    private boolean checkSecure(String hash, String salt) {
        if (hash == null || salt == null) return false;
        byte[] normal_hash = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
                config.getRuntime().getClientCheckSecret().concat(".").concat(salt));
        byte[] launcher_hash = Base64.getDecoder().decode(hash);
        return Arrays.equals(normal_hash, launcher_hash);
    }

    public static class LauncherTokenVerifier implements RestoreResponseService.ExtendedTokenProvider {

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
                client.type = AuthResponse.ConnectTypes.CLIENT;
                return true;
            } catch (Exception e) {
                logger.error("JWT multiModCheck failed", e);
                return false;
            }

        }
    }
}
