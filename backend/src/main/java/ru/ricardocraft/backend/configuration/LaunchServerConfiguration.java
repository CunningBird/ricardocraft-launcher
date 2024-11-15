package ru.ricardocraft.backend.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.AuthProviders;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.base.events.request.LauncherRequestEvent;
import ru.ricardocraft.backend.core.LauncherTrustManager;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.helper.JVMHelper;
import ru.ricardocraft.backend.manangers.*;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.LaunchServerEnv;
import ru.ricardocraft.backend.service.auth.RestoreResponseService;
import ru.ricardocraft.backend.service.update.LauncherResponseService;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class LaunchServerConfiguration {

    private static final Logger logger = LogManager.getLogger();

    private final Path dir = IOHelper.WORKING_DIR;

    @PostConstruct
    public void init() {
        JVMHelper.verifySystemProperties(LaunchServer.class, false);
        Security.addProvider(new BouncyCastleProvider());
    }

    @Bean
    public LaunchServerEnv getEnv() {
        return LaunchServerEnv.PRODUCTION;
    }

    @Bean
    public Map<String, RestoreResponseService.ExtendedTokenProvider> restoreProviders(AuthProviders authProviders,
                                                                                      AuthManager authManager,
                                                                                      KeyAgreementManager keyAgreementManager) {
        Map<String, RestoreResponseService.ExtendedTokenProvider> restoreProviders = new HashMap<>();
        restoreProviders.put(LauncherRequestEvent.LAUNCHER_EXTENDED_TOKEN_NAME, new LauncherResponseService.LauncherTokenVerifier(keyAgreementManager));
        restoreProviders.put("publicKey", new AdvancedProtectHandler.PublicKeyTokenVerifier(keyAgreementManager));
        restoreProviders.put("hardware", new AdvancedProtectHandler.HardwareInfoTokenVerifier(keyAgreementManager));
        restoreProviders.put("checkServer", new AuthManager.CheckServerVerifier(authManager, authProviders));
        return restoreProviders;
    }

    @Bean
    public CertificateManager certificateManager() throws IOException {
        CertificateManager certificateManager = new CertificateManager();
        try {
            certificateManager.readTrustStore(dir.resolve("truststore"));
        } catch (CertificateException e) {
            throw new IOException(e);
        }
        {
            LauncherTrustManager.CheckClassResult result = certificateManager.checkClass(LaunchServer.class);
            if (result.type == LauncherTrustManager.CheckClassResultType.SUCCESS) {
                logger.info("LaunchServer signed by {}", result.endCertificate.getSubjectX500Principal().getName());
            } else if (result.type == LauncherTrustManager.CheckClassResultType.NOT_SIGNED) {
                // None
            } else {
                if (result.exception != null) {
                    logger.error(result.exception);
                }
                logger.warn("LaunchServer signed incorrectly. Status: {}", result.type.name());
            }
        }

        return new CertificateManager();
    }

    @Bean
    public LaunchServerConfigManager launchServerConfigManager() {
        Path configFile = dir.resolve("LaunchServer.json");

        return new BasicLaunchServerConfigManager(configFile);
    }

    @Bean
    public KeyAgreementManager keyAgreementManager(LaunchServerDirectories directories) throws IOException, InvalidKeySpecException {
        return new KeyAgreementManager(directories.keyDirectory);
    }
}
