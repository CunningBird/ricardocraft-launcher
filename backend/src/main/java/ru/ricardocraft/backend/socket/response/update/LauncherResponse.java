package ru.ricardocraft.backend.socket.response.update;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.events.request.LauncherRequestEvent;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.socket.response.auth.RestoreResponse;
import ru.ricardocraft.backend.utils.Version;
import ru.ricardocraft.backend.helper.SecurityHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class LauncherResponse extends SimpleResponse {
    public Version version;
    public String hash;
    public byte[] digest;
    public int launcher_type;

    public String secureHash;
    public String secureSalt;

    @Override
    public String getType() {
        return "launcher";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        byte[] bytes;
        if (hash != null)
            bytes = Base64.getDecoder().decode(hash);
        else
            bytes = digest;
        if (launcher_type == 1) // JAR
        {
            byte[] hash = launcherBinary.getDigest();
            if (hash == null)
                service.sendObjectAndClose(ctx, new LauncherRequestEvent(true, config.netty.launcherURL));
            if (Arrays.equals(bytes, hash) && checkSecure(secureHash, secureSalt)) {
                client.checkSign = true;
                sendResult(new LauncherRequestEvent(false, config.netty.launcherURL, createLauncherExtendedToken(), config.netty.security.launcherTokenExpire*1000));
            } else {
                sendResultAndClose(new LauncherRequestEvent(true, config.netty.launcherURL, null, 0));
            }
        } else if (launcher_type == 2) //EXE
        {
            byte[] hash = exeLauncherBinary.getDigest();
            if (hash == null) sendResultAndClose(new LauncherRequestEvent(true, config.netty.launcherEXEURL));
            if (Arrays.equals(bytes, hash) && checkSecure(secureHash, secureSalt)) {
                client.checkSign = true;
                sendResult(new LauncherRequestEvent(false, config.netty.launcherEXEURL, createLauncherExtendedToken(), config.netty.security.launcherTokenExpire*1000));
            } else {
                sendResultAndClose(new LauncherRequestEvent(true, config.netty.launcherEXEURL, null, 0));
            }
        } else sendError("Request launcher type error");
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
                runtimeConfig.clientCheckSecret.concat(".").concat(salt));
        byte[] launcher_hash = Base64.getDecoder().decode(hash);
        return Arrays.equals(normal_hash, launcher_hash);
    }

    public static class LauncherTokenVerifier implements RestoreResponse.ExtendedTokenProvider {
        private final JwtParser parser;
        private final Logger logger = LogManager.getLogger();

        public LauncherTokenVerifier(LaunchServer server) {
            parser = Jwts.parser()
                    .verifyWith(server.keyAgreementManager.ecdsaPublicKey)
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

    @Override
    public ThreadSafeStatus getThreadSafeStatus() {
        return ThreadSafeStatus.READ_WRITE;
    }

}
