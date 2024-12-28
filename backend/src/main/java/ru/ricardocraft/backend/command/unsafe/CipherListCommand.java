package ru.ricardocraft.backend.command.unsafe;

import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.security.Provider;
import java.security.Security;

@Slf4j
@ShellComponent
@ShellCommandGroup("unsafe")
public class CipherListCommand {

    @ShellMethod("[] list all available ciphers.")
    public void loadJar() {
        for (Provider provider : Security.getProviders()) {
            log.info("Provider {} | {}", provider.getName(), provider.getClass().getName());
            for (Provider.Service service : provider.getServices()) {
                log.info("Service {} | alg {}", service.getClassName(), service.getAlgorithm());
            }
        }
    }
}
