package ru.ricardocraft.client.config;

import ru.ricardocraft.client.service.Launcher;
import ru.ricardocraft.client.service.LauncherTrustManager;
import ru.ricardocraft.client.base.serialize.HInput;
import ru.ricardocraft.client.base.serialize.HOutput;
import ru.ricardocraft.client.base.serialize.stream.StreamObject;
import ru.ricardocraft.client.base.helper.JVMHelper;
import ru.ricardocraft.client.base.helper.LogHelper;
import ru.ricardocraft.client.base.helper.SecurityHelper;
import ru.ricardocraft.client.base.helper.VerifyHelper;
import ru.ricardocraft.client.service.modules.LauncherModule;
import ru.ricardocraft.client.service.modules.LauncherModulesManager;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public final class LauncherConfig extends StreamObject {
    private static final List<byte[]> secureConfigCertificates = new ArrayList<>();
    private static final List<Class<?>> modulesClasses = new ArrayList<>();
    private static final MethodType VOID_TYPE = MethodType.methodType(void.class);
    public final String projectName;
    public final int clientPort;
    public final LauncherTrustManager trustManager;
    public final ECPublicKey ecdsaPublicKey;
    public final RSAPublicKey rsaPublicKey;
    public final Map<String, byte[]> runtime;
    public final String secureCheckHash;
    public final String secureCheckSalt;
    public final String passwordEncryptKey;
    public final String runtimeEncryptKey;
    public final String address;
    public String secretKeyClient;
    public String unlockSecret;
    public LauncherEnvironment environment;
    public long buildNumber;

    private static class ModernModulesClass {
        private static final List<Class<?>> modulesClasses = new ArrayList<>();
    }

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

    public static void initModules(LauncherModulesManager modulesManager) {
        if (JVMHelper.JVM_VERSION >= 17) {
            modulesClasses.addAll(ModernModulesClass.modulesClasses);
        }
        for (Class<?> clazz : modulesClasses)
            try {
                modulesManager.loadModule((LauncherModule) MethodHandles.publicLookup().findConstructor(clazz, VOID_TYPE).invokeWithArguments(Collections.emptyList()));
            } catch (Throwable e) {
                LogHelper.error(e);
            }
        // This method should be called once at exec time.
        modulesClasses.clear();
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

    public enum LauncherEnvironment {
        DEV,
        DEBUG,
        STD,
        PROD
    }
}
