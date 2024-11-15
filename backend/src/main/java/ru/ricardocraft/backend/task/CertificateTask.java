package ru.ricardocraft.backend.task;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.ricardocraft.backend.helper.SignHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.time.Instant;

@Configuration
@EnableScheduling
// TODO on config.sign.checkCertificateExpired && config.sign.enabled property
//@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class CertificateTask {

    private final Logger logger = LogManager.getLogger();

    private final LaunchServerConfig config;

    @Scheduled(fixedDelayString = "P1D")
    public void checkCertificateExpired() {
        if (!config.sign.enabled) {
            return;
        }
        try {
            KeyStore keyStore = SignHelper.getStore(Paths.get(config.sign.keyStore), config.sign.keyStorePass, config.sign.keyStoreType);
            Instant date = SignHelper.getCertificateExpired(keyStore, config.sign.keyAlias);
            if (date == null) {
                logger.debug("The certificate will expire at unlimited");
            } else if (date.minus(Duration.ofDays(30)).isBefore(Instant.now())) {
                logger.warn("The certificate will expire at {}", date.toString());
            } else {
                logger.debug("The certificate will expire at {}", date.toString());
            }
        } catch (Exception e) {
            logger.error("Can't get certificate expire date {}", e.getMessage());
        }
    }
}
