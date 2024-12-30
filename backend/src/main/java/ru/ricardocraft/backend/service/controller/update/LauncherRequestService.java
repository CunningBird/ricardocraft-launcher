package ru.ricardocraft.backend.service.controller.update;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.Client;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.dto.request.update.LauncherRequest;
import ru.ricardocraft.backend.dto.response.update.LauncherResponse;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.service.KeyAgreementService;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.controller.auth.RestoreRequestService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class LauncherRequestService {

    private final HttpServerProperties httpServerProperties;
    private final KeyAgreementService keyAgreementService;

    public LauncherResponse launcher(LauncherRequest request, Client client) throws Exception {
        byte[] bytes;
        if (request.hash != null)
            bytes = Base64.getDecoder().decode(request.hash);
        else
            bytes = new byte[]{};

        if (request.launcher_type == 1) {
            byte[] hash = getLauncherJarHash(bytes);
            if (hash == null) {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherURL());
                res.closeChannel = true;
                return res;
            }
            if (Arrays.equals(bytes, hash) && checkSecure(request.secureHash, request.secureSalt)) {
                client.checkSign = true;
                return new LauncherResponse(false, httpServerProperties.getLauncherURL(), createLauncherExtendedToken(), httpServerProperties.getSecurity().getLauncherTokenExpire() * 1000);
            } else {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherURL(), null, 0);
                res.closeChannel = true;
                return res;
            }
        } else if (request.launcher_type == 2) { // EXE
            byte[] hash = getLauncherExeHash(bytes);
            if (hash == null) {
                LauncherResponse res = new LauncherResponse(true, httpServerProperties.getLauncherEXEURL());
                res.closeChannel = true;
                return res;
            }
            if (Arrays.equals(bytes, hash) && checkSecure(request.secureHash, request.secureSalt)) {
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
                .signWith(keyAgreementService.ecdsaPrivateKey, SignatureAlgorithm.ES256)
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

    @Slf4j
    public static class LauncherTokenVerifier implements RestoreRequestService.ExtendedTokenProvider {

        private final JwtParser parser;

        public LauncherTokenVerifier(KeyAgreementService keyAgreementService) {
            parser = Jwts.parser()
                    .verifyWith(keyAgreementService.ecdsaPublicKey)
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
                log.error("JWT multiModCheck failed", e);
                return false;
            }

        }
    }
}
