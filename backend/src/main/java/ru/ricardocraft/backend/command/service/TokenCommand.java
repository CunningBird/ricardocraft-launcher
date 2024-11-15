package ru.ricardocraft.backend.command.service;

import io.jsonwebtoken.Jwts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.manangers.AuthManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;

@Component
public class TokenCommand extends Command {
    private transient final Logger logger = LogManager.getLogger();

    @Autowired
    public TokenCommand(AuthManager authManager,
                        KeyAgreementManager keyAgreementManager,
                        ProfileProvider profileProvider,
                        AuthProviders authProviders) {
        super();
        this.childCommands.put("info", new SubCommand("[token]", "print token info") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                var parser = Jwts.parser().verifyWith(keyAgreementManager.ecdsaPublicKey).build();
                var claims = parser.parseSignedClaims(args[0]);
                logger.info("Token: {}", claims.getPayload());
            }
        });
        this.childCommands.put("server", new SubCommand("[profileName] (authId) (public only)", "generate new server token") {
            @Override
            public void invoke(String... args) {
                AuthProviderPair pair = args.length > 1 ? authProviders.getAuthProviderPair(args[1]) : authProviders.getAuthProviderPair();
                boolean publicOnly = args.length <= 2 || Boolean.parseBoolean(args[2]);
                ClientProfile profile = null;
                for (ClientProfile p : profileProvider.getProfiles()) {
                    if (p.getTitle().equals(args[0]) || p.getUUID().toString().equals(args[0])) {
                        profile = p;
                        break;
                    }
                }
                if (profile == null) {
                    logger.warn("Profile {} not found", args[0]);
                }
                if (pair == null) {
                    logger.error("AuthId {} not found", args[1]);
                    return;
                }
                String token = authManager.newCheckServerToken(profile != null ? profile.getUUID().toString() : args[0], pair.name, publicOnly);
                logger.info("Server token {} authId {}: {}", args[0], pair.name, token);
            }
        });
    }

    @Override
    public String getArgsDescription() {
        return "[server/info/token name] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "jwt management";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
