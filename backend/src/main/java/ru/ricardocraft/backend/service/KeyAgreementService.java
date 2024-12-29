package ru.ricardocraft.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

@Slf4j
@Component
public class KeyAgreementService {

    public final ECPublicKey ecdsaPublicKey;
    public final ECPrivateKey ecdsaPrivateKey;
    public final RSAPublicKey rsaPublicKey;
    public final RSAPrivateKey rsaPrivateKey;
    public final String legacySalt;
    public final Path keyDirectory;

    @Autowired
    public KeyAgreementService(DirectoriesService directoriesService) throws IOException, InvalidKeySpecException {
        this.keyDirectory = directoriesService.getKeyDirectoryDir();
        Path ecdsaPublicKeyPath = keyDirectory.resolve("ecdsa_id.pub"), ecdsaPrivateKeyPath = keyDirectory.resolve("ecdsa_id");

        if (IOHelper.isFile(ecdsaPublicKeyPath) && IOHelper.isFile(ecdsaPrivateKeyPath)) {
            log.info("Reading ECDSA keypair");
            ecdsaPublicKey = SecurityHelper.toPublicECDSAKey(IOHelper.read(ecdsaPublicKeyPath));
            ecdsaPrivateKey = SecurityHelper.toPrivateECDSAKey(IOHelper.read(ecdsaPrivateKeyPath));
        } else {
            log.info("Generating ECDSA keypair");
            KeyPair pair = SecurityHelper.genECDSAKeyPair(new SecureRandom());
            ecdsaPublicKey = (ECPublicKey) pair.getPublic();
            ecdsaPrivateKey = (ECPrivateKey) pair.getPrivate();

            // Write key pair list
            log.info("Writing ECDSA keypair list");
            IOHelper.write(ecdsaPublicKeyPath, ecdsaPublicKey.getEncoded());
            IOHelper.write(ecdsaPrivateKeyPath, ecdsaPrivateKey.getEncoded());
        }
        Path rsaPublicKeyPath = keyDirectory.resolve("rsa_id.pub"), rsaPrivateKeyPath = keyDirectory.resolve("rsa_id");
        if (IOHelper.isFile(rsaPublicKeyPath) && IOHelper.isFile(rsaPrivateKeyPath)) {
            log.info("Reading RSA keypair");
            rsaPublicKey = SecurityHelper.toPublicRSAKey(IOHelper.read(rsaPublicKeyPath));
            rsaPrivateKey = SecurityHelper.toPrivateRSAKey(IOHelper.read(rsaPrivateKeyPath));
        } else {
            log.info("Generating RSA keypair");
            KeyPair pair = SecurityHelper.genRSAKeyPair(new SecureRandom());
            rsaPublicKey = (RSAPublicKey) pair.getPublic();
            rsaPrivateKey = (RSAPrivateKey) pair.getPrivate();

            // Write key pair list
            log.info("Writing RSA keypair list");
            IOHelper.write(rsaPublicKeyPath, rsaPublicKey.getEncoded());
            IOHelper.write(rsaPrivateKeyPath, rsaPrivateKey.getEncoded());
        }
        Path legacySaltPath = keyDirectory.resolve("legacySalt");
        if (IOHelper.isFile(legacySaltPath)) {
            legacySalt = new String(IOHelper.read(legacySaltPath), StandardCharsets.UTF_8);
        } else {
            legacySalt = SecurityHelper.randomStringToken();
            IOHelper.write(legacySaltPath, legacySalt.getBytes(StandardCharsets.UTF_8));
        }
    }
}
