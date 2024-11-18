package ru.ricardocraft.backend.command.mirror.token;

import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;

@Component
public class TokenInfoCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(TokenInfoCommand.class);

    private final KeyAgreementManager keyAgreementManager;

    public TokenInfoCommand(final KeyAgreementManager keyAgreementManager) {
        this.keyAgreementManager = keyAgreementManager;
    }

    @Override
    public String getArgsDescription() {
        return "[token]";
    }

    @Override
    public String getUsageDescription() {
        return "print token info";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        var parser = Jwts.parser().verifyWith(keyAgreementManager.ecdsaPublicKey).build();
        var claims = parser.parseSignedClaims(args[0]);
        logger.info("Token: {}", claims.getPayload());
    }
}
