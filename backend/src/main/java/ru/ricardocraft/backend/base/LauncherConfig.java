package ru.ricardocraft.backend.base;

import ru.ricardocraft.backend.core.LauncherInject;
import ru.ricardocraft.backend.core.LauncherInjectionConstructor;
import ru.ricardocraft.backend.core.LauncherTrustManager;
import ru.ricardocraft.backend.core.serialize.HInput;
import ru.ricardocraft.backend.core.serialize.HOutput;
import ru.ricardocraft.backend.core.serialize.stream.StreamObject;
import ru.ricardocraft.backend.helper.SecurityHelper;
import ru.ricardocraft.backend.helper.VerifyHelper;
import ru.ricardocraft.backend.properties.LauncherEnvironment;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public final class LauncherConfig extends StreamObject {
    @LauncherInject("launchercore.certificates")
    private static final List<byte[]> secureConfigCertificates = null;
    @LauncherInject("launcher.projectName")
    public final String projectName;
    @LauncherInject("launcher.port")
    public final int clientPort;
    public final LauncherTrustManager trustManager;
    public final ECPublicKey ecdsaPublicKey;
    public final RSAPublicKey rsaPublicKey;
    public final Map<String, byte[]> runtime;
    @LauncherInject("runtimeconfig.secureCheckHash")
    public final String secureCheckHash;
    @LauncherInject("runtimeconfig.secureCheckSalt")
    public final String secureCheckSalt;
    @LauncherInject("runtimeconfig.passwordEncryptKey")
    public final String passwordEncryptKey;
    @LauncherInject("runtimeconfig.runtimeEncryptKey")
    public final String runtimeEncryptKey;
    @LauncherInject("launcher.address")
    public final String address;
    @LauncherInject("runtimeconfig.secretKeyClient")
    public String secretKeyClient;
    @LauncherInject("runtimeconfig.unlockSecret")
    public String unlockSecret;
    @LauncherInject("launchercore.env")
    public LauncherEnvironment environment;
    @LauncherInject("runtimeconfig.buildNumber")
    public long buildNumber;

    @LauncherInjectionConstructor
    public LauncherConfig(HInput input) throws IOException, InvalidKeySpecException {
        ecdsaPublicKey = SecurityHelper.toPublicECDSAKey(input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH));
        rsaPublicKey = SecurityHelper.toPublicRSAKey(input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH));
        secureCheckHash = null;
        secureCheckSalt = null;
        passwordEncryptKey = null;
        runtimeEncryptKey = null;
        projectName = null;
        clientPort = -1;
        secretKeyClient = null;
        try {
            trustManager = new LauncherTrustManager(secureConfigCertificates);
        } catch (CertificateException e) {
            throw new IOException(e);
        }
        address = null;
        environment = LauncherEnvironment.STD;
        Launcher.applyLauncherEnv(environment);
        // Read signed runtime
        int count = input.readLength(0);
        Map<String, byte[]> localResources = new HashMap<>(count);
        for (int i = 0; i < count; i++) {
            String name = input.readString(255);
            VerifyHelper.putIfAbsent(localResources, name,
                    input.readByteArray(SecurityHelper.CRYPTO_MAX_LENGTH),
                    String.format("Duplicate runtime resource: '%s'", name));
        }
        runtime = Collections.unmodifiableMap(localResources);
    }

    public LauncherConfig(String address, ECPublicKey ecdsaPublicKey, RSAPublicKey rsaPublicKey, Map<String, byte[]> runtime, String projectName) {
        this.address = address;
        this.ecdsaPublicKey = ecdsaPublicKey;
        this.rsaPublicKey = rsaPublicKey;
        this.runtime = Map.copyOf(runtime);
        this.projectName = projectName;
        this.clientPort = 32148;
        environment = LauncherEnvironment.STD;
        secureCheckSalt = null;
        secureCheckHash = null;
        passwordEncryptKey = null;
        runtimeEncryptKey = null;
        trustManager = null;
    }

    public LauncherConfig(String address, Map<String, byte[]> runtime, String projectName, LauncherEnvironment env, LauncherTrustManager trustManager) {
        this.address = address;
        this.runtime = Map.copyOf(runtime);
        this.projectName = projectName;
        this.clientPort = 32148;
        this.trustManager = trustManager;
        this.rsaPublicKey = null;
        this.ecdsaPublicKey = null;
        environment = env;
        secureCheckSalt = null;
        secureCheckHash = null;
        passwordEncryptKey = null;
        runtimeEncryptKey = null;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeByteArray(ecdsaPublicKey.getEncoded(), SecurityHelper.CRYPTO_MAX_LENGTH);
        output.writeByteArray(rsaPublicKey.getEncoded(), SecurityHelper.CRYPTO_MAX_LENGTH);

        // Write signed runtime
        Set<Map.Entry<String, byte[]>> entrySet = runtime.entrySet();
        output.writeLength(entrySet.size(), 0);
        for (Map.Entry<String, byte[]> entry : runtime.entrySet()) {
            output.writeString(entry.getKey(), 255);
            output.writeByteArray(entry.getValue(), SecurityHelper.CRYPTO_MAX_LENGTH);
        }
    }
}
