package ru.ricardocraft.backend.task;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import ru.ricardocraft.backend.binary.tasks.sign.SignerJar;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "launch-server-config.sign.checkCertificateExpired")
@RequiredArgsConstructor
public class CertificateTask {

    private final Logger logger = LogManager.getLogger(CertificateTask.class);

    private final LaunchServerProperties config;

    @Scheduled(fixedDelayString = "P1D")
    public void checkCertificateExpired() {
        if (!config.getSign().getEnabled()) {
            return;
        }
        try {
            KeyStore keyStore = SignerJar.getStore(Paths.get(config.getSign().getKeyStore()), config.getSign().getKeyStorePass(), config.getSign().getKeyStoreType());
            Instant date = getCertificateExpired(keyStore, config.getSign().getKeyAlias());
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

    private Instant getCertificateExpired(KeyStore keyStore, String keyAlias) throws KeyStoreException {
        List<Certificate> certChain = new ArrayList<>(Arrays.asList(keyStore.getCertificateChain(keyAlias)));
        Date date = null;
        for(var e : certChain) {
            if(e instanceof X509Certificate x509Certificate) {
                if(x509Certificate.getNotAfter() == null) {
                    continue;
                }
                if(date == null || date.after(x509Certificate.getNotAfter())) {
                    date = x509Certificate.getNotAfter();
                }
            }
        }
        return date == null ? null : date.toInstant();
    }

}
