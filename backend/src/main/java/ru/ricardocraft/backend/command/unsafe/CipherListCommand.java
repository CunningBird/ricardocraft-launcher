package ru.ricardocraft.backend.command.unsafe;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.base.helper.LogHelper;

import java.security.Provider;
import java.security.Security;

@Component
@NoArgsConstructor
public class CipherListCommand extends Command {

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
            LogHelper.info("Provider %s | %s", provider.getName(), provider.getClass().getName());
            for (Provider.Service service : provider.getServices()) {
                LogHelper.subInfo("Service %s | alg %s", service.getClassName(), service.getAlgorithm());
            }
        }
    }
}
