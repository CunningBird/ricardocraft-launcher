package ru.ricardocraft.backend.command.unsafe;

import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;

import java.security.Provider;
import java.security.Security;

@Component
@NoArgsConstructor
public class CipherListCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(CipherListCommand.class);

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "list all available ciphers";
    }

    @Override
    public void invoke(String... args) {
        for (Provider provider : Security.getProviders()) {
            logger.info("Provider {} | {}", provider.getName(), provider.getClass().getName());
            for (Provider.Service service : provider.getServices()) {
                logger.info("Service {} | alg {}", service.getClassName(), service.getAlgorithm());
            }
        }
    }
}
