package ru.ricardocraft.backend.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.*;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class GenerateCertificateCommand extends Command {

    private final Logger logger = LogManager.getLogger(GenerateCertificateCommand.class);

    private transient final LaunchServerProperties config;
    private transient final DirectoriesManager directoriesManager;
    private transient final CertificateManager certificateManager;
    private transient final KeyAgreementManager keyAgreementManager;

    @Autowired
    public GenerateCertificateCommand(CommandHandler commandHandler,
                                      LaunchServerProperties config,
                                      DirectoriesManager directoriesManager,
                                      CertificateManager certificateManager,
                                      KeyAgreementManager keyAgreementManager) {
        super();
        this.config = config;
        this.directoriesManager = directoriesManager;
        this.certificateManager = certificateManager;
        this.keyAgreementManager = keyAgreementManager;

        commandHandler.registerCommand("generatecertificate", this);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "Generate self-signed certificate";
    }

    @Override
    public void invoke(String... args) throws Exception {
        String projectName = config.getProjectName();
        Path targetDir = directoriesManager.getKeyDirectoryDir().resolve("certs");
        Path rootCACrtPath = targetDir.resolve(projectName.concat("RootCA.crt"));
        Path rootCAKeyPath = targetDir.resolve(projectName.concat("RootCA.key"));
        Path codeSignCrtPath = targetDir.resolve(projectName.concat("CodeSign.crt"));
        Path codeSignKeyPath = targetDir.resolve(projectName.concat("CodeSign.key"));
        Path p12FilePath = targetDir.resolve(projectName.concat("CodeSign.p12"));

        logger.info("Generate certificates for project {}", projectName);
        LocalDateTime startDate = LocalDate.now().atStartOfDay();
        logger.info("Generate CA Certificate");
        GeneratedCertificate rootCA = generateRootCA(projectName, startDate);
        logger.info("Generate ending certificate");
        GeneratedCertificate endCert = generateEndCertificate(projectName, rootCA.certificate().getSubject(), rootCA.pair.getPrivate(), startDate);
        logger.info("Save certificates to disk");
        certificateManager.writeCertificate(rootCACrtPath, rootCA.certificate());
        certificateManager.writePrivateKey(rootCAKeyPath, rootCA.pair().getPrivate());

        certificateManager.writeCertificate(codeSignCrtPath, rootCA.certificate());
        certificateManager.writePrivateKey(codeSignKeyPath, rootCA.pair().getPrivate());

        logger.info("Prepare PKCS#12 keystore");
        String passwd = SecurityHelper.randomStringToken();
        PKCS12PfxPdu pfx = makePkcs12(endCert, rootCA, projectName, passwd);
        logger.info("Save PKCS#12 keystore");
        try (OutputStream output = IOHelper.newOutput(p12FilePath)) {
            output.write(pfx.getEncoded());
        }
        logger.info("Generate sign config");
        logger.warn("Must save your store password");
        if (!config.getSign().getEnabled()) {
            logger.info("Write config");
            logger.info("Add your RootCA to truststore");
            Path pathToRootCA = directoriesManager.getTrustStoreDir().resolve(projectName.concat("RootCA.crt"));
            Files.deleteIfExists(pathToRootCA);
            Files.copy(rootCACrtPath, pathToRootCA);
            certificateManager.readTrustStore(targetDir.resolve("truststore"));
        } else {
            Path pathToRootCA = targetDir.resolve("truststore").resolve(projectName.concat("RootCA.crt"));
            Files.deleteIfExists(pathToRootCA);
            Files.copy(rootCACrtPath, pathToRootCA);
        }
        //caKey = PrivateKeyFactory.createKey(pair.getPrivate().getEncoded());
    }

    private static PKCS12PfxPdu makePkcs12(GeneratedCertificate endCert, GeneratedCertificate rootCA, String projectName, String passwd) throws NoSuchAlgorithmException, IOException, PKCSException {
        PublicKey codeSignPublicKey = endCert.pair().getPublic();
        PrivateKey codeSignPrivateKey = endCert.pair().getPrivate();
        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        PKCS12PfxPduBuilder pkcsBuilder = new PKCS12PfxPduBuilder();
        PKCS12SafeBagBuilder caCertBagBuilder = new PKCS12SafeBagBuilder(rootCA.certificate());
        caCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(projectName.concat("RootCA")));
        PKCS12SafeBagBuilder endingCertBagBuilder = new PKCS12SafeBagBuilder(endCert.certificate());
        endingCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(projectName.concat("CodeSign")));
        endingCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(codeSignPublicKey));
        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(codeSignPrivateKey, new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, new CBCBlockCipher(new DESedeEngine())).build(passwd.toCharArray()));
        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString(projectName.concat("CodeSign")));
        keyBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(codeSignPublicKey));
        PKCS12SafeBag[] certs = new PKCS12SafeBag[2];
        certs[1] = caCertBagBuilder.build();
        certs[0] = endingCertBagBuilder.build();
        pkcsBuilder.addData(keyBagBuilder.build());
        pkcsBuilder.addEncryptedData(new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC, new CBCBlockCipher(new DESedeEngine())).build(passwd.toCharArray()), certs);
        return pkcsBuilder.build(new BcPKCS12MacCalculatorBuilder(), passwd.toCharArray());
    }

    private static @NotNull GeneratedCertificate generateEndCertificate(String projectName, X500Name subject, PrivateKey issuerPrivateKey, LocalDateTime startDate) throws CertIOException, OperatorCreationException, NoSuchAlgorithmException {
        SecureRandom random = SecurityHelper.newRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, random);
        KeyPair endingPair = generator.generateKeyPair();
        X500NameBuilder endingSubject = new X500NameBuilder();
        endingSubject.addRDN(BCStyle.CN, projectName.concat(" Code Sign"));
        endingSubject.addRDN(BCStyle.O, projectName);

        X509v3CertificateBuilder endingBuilder = new X509v3CertificateBuilder(
                subject,
                BigInteger.valueOf(random.nextLong()),
                Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(startDate.plusDays(365).atZone(ZoneId.systemDefault()).toInstant()),
                endingSubject.build(),
                SubjectPublicKeyInfo.getInstance(endingPair.getPublic().getEncoded()));
        ExtendedKeyUsage extendedKeyUsage = new ExtendedKeyUsage(new KeyPurposeId[]{KeyPurposeId.id_kp_codeSigning});
        endingBuilder.addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage);
        endingBuilder.addExtension(Extension.keyUsage, false, new KeyUsage(KeyUsage.digitalSignature));
        JcaContentSignerBuilder endingCsBuilder = new JcaContentSignerBuilder("SHA256WITHRSA");
        ContentSigner endingSigner = endingCsBuilder.build(issuerPrivateKey);
        X509CertificateHolder endingCertificate = endingBuilder.build(endingSigner);
        return new GeneratedCertificate(endingPair, endingCertificate);
    }

    private static GeneratedCertificate generateRootCA(String projectName, LocalDateTime startDate) throws NoSuchAlgorithmException, CertIOException, OperatorCreationException {
        SecureRandom random = SecurityHelper.newRandom();
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, random);
        KeyPair pair = generator.generateKeyPair();

        X500NameBuilder subject = new X500NameBuilder();
        subject.addRDN(BCStyle.CN, projectName.concat(" Root CA"));
        subject.addRDN(BCStyle.O, projectName);

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                subject.build(),
                BigInteger.valueOf(random.nextLong()),
                Date.from(startDate.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(startDate.plusDays(3650).atZone(ZoneId.systemDefault()).toInstant()),
                subject.build(),
                SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded()));
        builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(0));
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256WITHRSA");
        ContentSigner signer = csBuilder.build(pair.getPrivate());
        X509CertificateHolder caCertificate = builder.build(signer);
        return new GeneratedCertificate(pair, caCertificate);
    }

    private record GeneratedCertificate(KeyPair pair, X509CertificateHolder certificate) {
    }
}
