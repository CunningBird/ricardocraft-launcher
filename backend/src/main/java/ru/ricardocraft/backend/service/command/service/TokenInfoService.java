package ru.ricardocraft.backend.service.command.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.KeyAgreementService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenInfoService {

    private final KeyAgreementService keyAgreementService;

    public void tokenInfo(String token) {
        var parser = Jwts.parser().verifyWith(keyAgreementService.ecdsaPublicKey).build();
        var claims = parser.parseSignedClaims(token);
        log.info("Token: {}", claims.getPayload());
    }
}
