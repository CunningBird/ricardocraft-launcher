package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SignHelper;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.binary.tasks.sign.SignerJar;
import ru.ricardocraft.backend.properties.config.JarSignerProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SignJarTask implements LauncherBuildTask {

    private static final Logger logger = LogManager.getLogger(SignJarTask.class);

    private final JarSignerProperties config;
    private final JarLauncherBinary launcherBinary;

    public SignJarTask(JarLauncherBinary launcherBinary, JarSignerProperties config) {
        this.config = config;
        this.launcherBinary = launcherBinary;
    }

    public static CMSSignedDataGenerator gen(JarSignerProperties config, KeyStore c) {
        try {
            return SignHelper.createSignedDataGenerator(c,
                    config.getKeyAlias(), config.getSignAlgo(), config.getKeyPass());
        } catch (CertificateEncodingException | UnrecoverableKeyException | KeyStoreException
                 | OperatorCreationException | NoSuchAlgorithmException | CMSException e) {
            logger.error("Create signedDataGenerator failed", e);
            return null;
        }
    }

    @Override
    public String getName() {
        return "SignJar";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        Path toRet = launcherBinary.nextPath("signed");
        sign(config, inputFile, toRet);
        return toRet;
    }

    public void sign(JarSignerProperties config, Path inputFile, Path signedFile) throws IOException {
        if (config.getEnabled()) stdSign(config, inputFile, signedFile);
        else autoSign(inputFile, signedFile);
    }

    private void stdSign(JarSignerProperties config, Path inputFile, Path signedFile) throws IOException {
        KeyStore c = SignHelper.getStore(new File(config.getKeyStore()).toPath(), config.getKeyStorePass(), config.getKeyStoreType());
        try (SignerJar output = new SignerJar(new ZipOutputStream(IOHelper.newOutput(signedFile)), () -> SignJarTask.gen(config, c),
                config.getMetaInfSfName(), config.getMetaInfKeyName());
             ZipInputStream input = new ZipInputStream(IOHelper.newInput(inputFile))) {
            ZipEntry e = input.getNextEntry();
            while (e != null) {
                if ("META-INF/MANIFEST.MF".equals(e.getName()) || "/META-INF/MANIFEST.MF".equals(e.getName())) {
                    Manifest m = new Manifest(input);
                    m.getMainAttributes().forEach((a, b) -> output.addManifestAttribute(a.toString(), b.toString()));
                    e = input.getNextEntry();
                    continue;
                }
                output.addFileContents(IOHelper.newZipEntry(e), input);
                e = input.getNextEntry();
            }
        }
    }

    private void autoSign(Path inputFile, Path signedFile) throws IOException {
        try (SignerJar output = new SignerJar(new ZipOutputStream(IOHelper.newOutput(signedFile)), () -> {
            CertificateAutogenTask task = launcherBinary.getTaskByClass(CertificateAutogenTask.class).get();
            return task.signedDataGenerator;
        },
                "AUTOGEN.SF", "AUTOGEN.EC");
             ZipInputStream input = new ZipInputStream(IOHelper.newInput(inputFile))) {
            ZipEntry e = input.getNextEntry();
            while (e != null) {
                if ("META-INF/MANIFEST.MF".equals(e.getName()) || "/META-INF/MANIFEST.MF".equals(e.getName())) {
                    Manifest m = new Manifest(input);
                    m.getMainAttributes().forEach((a, b) -> output.addManifestAttribute(a.toString(), b.toString()));
                    e = input.getNextEntry();
                    continue;
                }
                output.addFileContents(IOHelper.newZipEntry(e), input);
                e = input.getNextEntry();
            }
        }
    }
}
