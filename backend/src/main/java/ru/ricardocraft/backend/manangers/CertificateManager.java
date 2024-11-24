package ru.ricardocraft.backend.manangers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.base.helper.IOHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CertificateManager {

    private transient final Logger logger = LogManager.getLogger(CertificateManager.class);

    public LauncherTrustManager trustManager;

    @Autowired
    public CertificateManager(DirectoriesManager directoriesManager) throws CertificateException, IOException {
        readTrustStore(directoriesManager.getTrustStoreDir());
        LauncherTrustManager.CheckClassResult result = checkClass(LaunchServer.class);
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

    public void writePrivateKey(Path file, PrivateKey privateKey) throws IOException {
        writePrivateKey(IOHelper.newWriter(file), privateKey);
    }

    public void writePrivateKey(Writer writer, PrivateKey privateKey) throws IOException {
        try (PemWriter writer1 = new PemWriter(writer)) {
            writer1.writeObject(new PemObject("PRIVATE KEY", privateKey.getEncoded()));
        }
    }

    public void writeCertificate(Path file, X509CertificateHolder holder) throws IOException {
        writeCertificate(IOHelper.newWriter(file), holder);
    }

    public void writeCertificate(Writer writer, X509CertificateHolder holder) throws IOException {
        try (PemWriter writer1 = new PemWriter(writer)) {
            writer1.writeObject(new PemObject("CERTIFICATE", holder.toASN1Structure().getEncoded()));
        }
    }

    public void readTrustStore(Path dir) throws IOException, CertificateException {
        if (!IOHelper.isDir(dir)) {
            Files.createDirectories(dir);
            try {
                URL inBuildCert = ResourceUtils.getFile("classpath:defaults/BuildCertificate.crt").toURL();
                try (OutputStream outputStream = IOHelper.newOutput(dir.resolve("BuildCertificate.crt"));
                     InputStream inputStream = IOHelper.newInput(inBuildCert)) {
                    IOHelper.transfer(inputStream, outputStream);
                }
            } catch (NoSuchFileException ignored) {

            }

        } else {
            if (IOHelper.exists(dir.resolve("GravitCentralRootCA.crt"))) {
                logger.warn("Found old default certificate - 'GravitCentralRootCA.crt'. Delete...");
                Files.delete(dir.resolve("GravitCentralRootCA.crt"));
            }
        }
        List<X509Certificate> certificates = new ArrayList<>();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        IOHelper.walk(dir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".crt")) {
                    try (InputStream inputStream = IOHelper.newInput(file)) {
                        certificates.add((X509Certificate) certFactory.generateCertificate(inputStream));
                    } catch (CertificateException e) {
                        throw new IOException(e);
                    }
                }
                return super.visitFile(file, attrs);
            }
        }, false);
        trustManager = new LauncherTrustManager(certificates.toArray(new X509Certificate[0]));
    }

    public LauncherTrustManager.CheckClassResult checkClass(Class<?> clazz) {
        X509Certificate[] certificates = getCertificates(clazz);
        return trustManager.checkCertificates(certificates, trustManager::stdCertificateChecker);
    }

    private X509Certificate[] getCertificates(Class<?> clazz) {
        Object[] signers = clazz.getSigners();
        if (signers == null) return null;
        return Arrays.stream(signers).filter((c) -> c instanceof X509Certificate).map((c) -> (X509Certificate) c).toArray(X509Certificate[]::new);
    }
}
