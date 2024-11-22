package ru.ricardocraft.backend.binary.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.binary.EXELauncherBinary;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.JarSignerProperties;
import ru.ricardocraft.backend.properties.config.OSSLSignCodeProperties;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

public class OSSLSignTask implements LauncherBuildTask {

    private static final Logger logger = LoggerFactory.getLogger(OSSLSignTask.class);

    private final EXELauncherBinary launcherEXEBinary;
    private final JarSignerProperties signConf;
    private final OSSLSignCodeProperties osslSignCodeConfig;

    public OSSLSignTask(EXELauncherBinary launcherEXEBinary, LaunchServerProperties config) {
        this.launcherEXEBinary = launcherEXEBinary;

        this.signConf = config.getSign();
        this.osslSignCodeConfig = config.getOsslSignCode();
        if (!signConf.getEnabled()) throw new IllegalStateException("sign.enabled must be true");
        if (!signConf.getKeyStoreType().equals("PKCS12"))
            throw new IllegalStateException("sign.keyStoreType must be PKCS12");
    }

    public static void signLaunch4j(OSSLSignCodeProperties config, JarSignerProperties signConf, Path inputFile, Path resultFile) throws IOException {
        File input = new File(inputFile.toUri());
        long lastSignSize = 0;
        long inputLength = input.length();
        Files.deleteIfExists(resultFile);
        updateSignSize(inputFile, lastSignSize);
        sign(config, signConf, inputFile, resultFile);
        File output = new File(resultFile.toUri());
        long outputLength = output.length();
        long signSize = outputLength - inputLength;
        if (lastSignSize != signSize) {
            logger.debug("Saved signSize value {}, real {}", lastSignSize, signSize);
            lastSignSize = signSize;
            Files.deleteIfExists(resultFile);
            updateSignSize(inputFile, signSize);
            sign(config, signConf, inputFile, resultFile);
            if (config.getCheckSignSize()) {
                output = new File(resultFile.toUri());
                outputLength = output.length();
                signSize = outputLength - inputLength;
                if (lastSignSize != signSize) {
                    throw new IllegalStateException("Sign multiModCheck size failed. Saved: %d Real: %d".formatted(lastSignSize, signSize));
                }
            }
            if (config.getCheckCorrectJar()) {
                try (ZipInputStream inputStream = IOHelper.newZipInput(resultFile)) {
                    inputStream.getNextEntry(); //Check
                }
            }
        }
    }

    public static void updateSignSize(Path inputFile, long signSize) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(new File(inputFile.toUri()), "rw")) {
            long fileSize = file.length();
            long offset = fileSize - 2;
            if (signSize > 0xffff) throw new IllegalArgumentException("Sign size > 65535");
            byte[] toWrite = new byte[]{(byte) (signSize & 0xff), (byte) ((signSize & 0xff00) >> 8)};
            logger.debug("File size {} offset {} first byte {} last byte {}", fileSize, offset, toWrite[0], toWrite[1]);
            file.seek(offset);
            file.write(toWrite);
        }
    }

    public static void sign(OSSLSignCodeProperties config, JarSignerProperties signConf, Path source, Path dest) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        List<String> args = new ArrayList<>();
        args.add(config.getOsslsigncodePath());
        args.add("sign");
        args.add("-pkcs12");
        args.add(signConf.getKeyStore());
        if (config.getTimestampServer() != null) {
            args.add("-t");
            args.add(config.getTimestampServer());
        }
        if (config.getCustomArgs() != null) args.addAll(config.getCustomArgs());
        if (signConf.getKeyPass() != null) {
            args.add("-pass");
            args.add(signConf.getKeyPass());
        }
        args.add("-in");
        args.add(source.toAbsolutePath().toString());
        args.add("-out");
        args.add(dest.toAbsolutePath().toString());
        builder.command(args);
        builder.inheritIO();
        Process process = builder.start();
        try {
            process.waitFor();
        } catch (InterruptedException ignored) {

        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("OSSLSignCode process return %d".formatted(process.exitValue()));
        }
    }

    @Override
    public String getName() {
        return "OSSLSign";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        Path resultFile = launcherEXEBinary.nextPath(getName());
        signLaunch4j(osslSignCodeConfig, signConf, inputFile, resultFile);
        return resultFile;
    }

    public void sign(Path source, Path dest) throws IOException {
        OSSLSignTask.sign(osslSignCodeConfig, signConf, source, dest);
    }
}
