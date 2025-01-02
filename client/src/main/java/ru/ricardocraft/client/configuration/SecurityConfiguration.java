package ru.ricardocraft.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.client.service.runtime.client.DirBridge;
import ru.ricardocraft.client.base.helper.IOHelper;
import ru.ricardocraft.client.base.helper.LogHelper;
import ru.ricardocraft.client.base.helper.SecurityHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

@Configuration
public class SecurityConfiguration {

    private final Path dir = DirBridge.dir;

    @Bean
    public ECPublicKey publicKey() throws IOException, InvalidKeySpecException {
        ECPublicKey publicKey;
        Path publicKeyFile = dir.resolve("public.key");
        if (IOHelper.isFile(publicKeyFile)) {
            LogHelper.info("Reading EC public key");
            publicKey = SecurityHelper.toPublicECDSAKey(IOHelper.read(publicKeyFile));
        } else {
            LogHelper.info("Generating EC public key");
            KeyPair pair = SecurityHelper.genECDSAKeyPair(new SecureRandom());
            publicKey = (ECPublicKey) pair.getPublic();

            // Write key pair list
            LogHelper.info("Writing EC public key");
            IOHelper.write(publicKeyFile, publicKey.getEncoded());
        }
        return publicKey;
    }

    @Bean
    public ECPrivateKey privateKey() throws IOException, InvalidKeySpecException {
        ECPrivateKey privateKey;
        Path privateKeyFile = dir.resolve("private.key");
        if (IOHelper.isFile(privateKeyFile)) {
            LogHelper.info("Reading EC private key");
            privateKey = SecurityHelper.toPrivateECDSAKey(IOHelper.read(privateKeyFile));
        } else {
            LogHelper.info("Generating EC private key");
            KeyPair pair = SecurityHelper.genECDSAKeyPair(new SecureRandom());
            privateKey = (ECPrivateKey) pair.getPrivate();

            // Write key pair list
            LogHelper.info("Writing EC private key");
            IOHelper.write(privateKeyFile, privateKey.getEncoded());
        }
        return privateKey;
    }
}
