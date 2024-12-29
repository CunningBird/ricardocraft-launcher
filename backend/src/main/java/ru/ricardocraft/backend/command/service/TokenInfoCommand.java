package ru.ricardocraft.backend.command.service;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.KeyAgreementService;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class TokenInfoCommand {

    private final KeyAgreementService keyAgreementService;

    @ShellMethod("[token] print token info")
    public void tokenInfo(@ShellOption String token) {
        var parser = Jwts.parser().verifyWith(keyAgreementService.ecdsaPublicKey).build();
        var claims = parser.parseSignedClaims(token);
        log.info("Token: {}", claims.getPayload());
    }
}
