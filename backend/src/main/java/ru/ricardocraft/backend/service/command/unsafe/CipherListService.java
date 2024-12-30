package ru.ricardocraft.backend.service.command.unsafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

import java.security.Provider;
import java.security.Security;

@Slf4j
@Component
public class CipherListService {

    public void loadJar() {
        for (Provider provider : Security.getProviders()) {
            log.info("Provider {} | {}", provider.getName(), provider.getClass().getName());
            for (Provider.Service service : provider.getServices()) {
                log.info("Service {} | alg {}", service.getClassName(), service.getAlgorithm());
            }
        }
    }
}
