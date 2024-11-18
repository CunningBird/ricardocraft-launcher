package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import ru.ricardocraft.backend.base.LauncherConfig;
import ru.ricardocraft.backend.base.asm.ClassMetadataReader;
import ru.ricardocraft.backend.base.asm.InjectClassAcceptor;
import ru.ricardocraft.backend.base.asm.SafeClassWriter;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.binary.BuildContext;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

public class MainBuildTask implements LauncherBuildTask {

    private transient final Logger logger = LogManager.getLogger(MainBuildTask.class);

    public final ClassMetadataReader reader;
    public final Set<String> blacklist = new HashSet<>();
    public final List<Transformer> transformers = new ArrayList<>();
    public final Map<String, Object> properties = new HashMap<>();

    private final JARLauncherBinary launcherBinary;
    private final LaunchServerConfig config;
    private final KeyAgreementManager keyAgreementManager;
    private final CertificateManager certificateManager;

    public static final String CONFIG_FILE = "config.bin";
    public static final String RUNTIME_DIR = "runtime";

    public MainBuildTask(JARLauncherBinary launcherBinary,
                         LaunchServerConfig config,
                         KeyAgreementManager keyAgreementManager,
                         CertificateManager certificateManager) {

        this.launcherBinary = launcherBinary;
        this.config = config;
        this.keyAgreementManager = keyAgreementManager;
        this.certificateManager = certificateManager;

        reader = new ClassMetadataReader();
        InjectClassAcceptor injectClassAcceptor = new InjectClassAcceptor(properties);
        transformers.add(injectClassAcceptor);
    }

    @Override
    public String getName() {
        return "MainBuild";
    }

    @Override
    public Path process(Path inputJar) throws IOException {
        Path outputJar = launcherBinary.nextPath(this);
        try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(outputJar))) {
            BuildContext context = new BuildContext(output, reader.getCp(), this, launcherBinary.runtimeDir);
            initProps();
            properties.put("launcher.legacymodules", context.legacyClientModules.stream().map(e -> Type.getObjectType(e.replace('.', '/'))).collect(Collectors.toList()));
            properties.put("launcher.modules", context.clientModules.stream().map(e -> Type.getObjectType(e.replace('.', '/'))).collect(Collectors.toList()));
            postInitProps();
            reader.getCp().add(new JarFile(inputJar.toFile()));
            for (Path e : launcherBinary.coreLibs) {
                reader.getCp().add(new JarFile(e.toFile()));
            }
            context.pushJarFile(inputJar, (e) -> blacklist.contains(e.getName()) || e.getName().startsWith("pro/gravit/launcher/debug/"), (e) -> true);

            // map for guard
            Map<String, byte[]> runtime = new HashMap<>(256);
            // Write launcher guard dir
            if (config.launcher.encryptRuntime) {
                context.pushEncryptedDir(context.getRuntimeDir(), RUNTIME_DIR, this.config.runtimeConfig.runtimeEncryptKey, runtime, false);
            } else {
                context.pushDir(context.getRuntimeDir(), RUNTIME_DIR, runtime, false);
            }
            if (context.isDeleteRuntimeDir()) {
                IOHelper.deleteDir(context.getRuntimeDir(), true);
            }

            LauncherConfig launcherConfig = new LauncherConfig(config.netty.address, keyAgreementManager.ecdsaPublicKey, keyAgreementManager.rsaPublicKey, runtime, config.projectName);
            context.pushFile(CONFIG_FILE, launcherConfig);
        }
        reader.close();
        return outputJar;
    }

    protected void postInitProps() {
        List<byte[]> certificates = Arrays.stream(certificateManager.trustManager.getTrusted()).map(e -> {
            try {
                return e.getEncoded();
            } catch (CertificateEncodingException e2) {
                logger.error("Certificate encoding failed", e2);
                return new byte[0];
            }
        }).collect(Collectors.toList());
        if (!config.sign.enabled) {
            CertificateAutogenTask task = launcherBinary.getTaskByClass(CertificateAutogenTask.class).get();
            try {
                certificates.add(task.certificate.getEncoded());
            } catch (CertificateEncodingException e) {
                throw new InternalError(e);
            }
        }
        properties.put("launchercore.certificates", certificates);
    }

    protected void initProps() {
        properties.clear();
        properties.put("launcher.address", config.netty.address);
        properties.put("launcher.projectName", config.projectName);
        properties.put("runtimeconfig.secretKeyClient", SecurityHelper.randomStringAESKey());
        properties.put("launcher.port", 32148 + SecurityHelper.newRandom().nextInt(512));
        properties.put("launchercore.env", config.env);
        properties.put("launcher.memory", config.launcher.memoryLimit);
        properties.put("launcher.customJvmOptions", config.launcher.customJvmOptions);
        if (config.launcher.encryptRuntime) {
            if (config.runtimeConfig.runtimeEncryptKey == null)
                config.runtimeConfig.runtimeEncryptKey = SecurityHelper.randomStringToken();
            properties.put("runtimeconfig.runtimeEncryptKey", config.runtimeConfig.runtimeEncryptKey);
        }
        properties.put("launcher.certificatePinning", config.launcher.certificatePinning);
        properties.put("runtimeconfig.passwordEncryptKey", config.runtimeConfig.passwordEncryptKey);
        String launcherSalt = SecurityHelper.randomStringToken();
        byte[] launcherSecureHash = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
                config.runtimeConfig.clientCheckSecret.concat(".").concat(launcherSalt));
        properties.put("runtimeconfig.secureCheckHash", Base64.getEncoder().encodeToString(launcherSecureHash));
        properties.put("runtimeconfig.secureCheckSalt", launcherSalt);
        if (config.runtimeConfig.unlockSecret == null) config.runtimeConfig.unlockSecret = SecurityHelper.randomStringToken();
        properties.put("runtimeconfig.unlockSecret", config.runtimeConfig.unlockSecret);
        config.runtimeConfig.buildNumber++;
        properties.put("runtimeconfig.buildNumber", config.runtimeConfig.buildNumber);
    }

    public byte[] transformClass(byte[] bytes, String classname, BuildContext context) {
        byte[] result = bytes;
        ClassWriter writer;
        ClassNode cn = null;
        for (Transformer t : transformers) {
            if (t instanceof ASMTransformer asmTransformer) {
                if (cn == null) {
                    ClassReader cr = new ClassReader(result);
                    cn = new ClassNode();
                    cr.accept(cn, 0);
                }
                asmTransformer.transform(cn, classname, context);
                continue;
            } else if (cn != null) {
                writer = new SafeClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                cn.accept(writer);
                result = writer.toByteArray();
            }
            byte[] old_result = result;
            result = t.transform(result, classname, context);
            if (old_result != result) {
                cn = null;
            }
        }
        if (cn != null) {
            writer = new SafeClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(writer);
            result = writer.toByteArray();
        }
        return result;
    }

    @FunctionalInterface
    public interface Transformer {
        byte[] transform(byte[] input, String classname, BuildContext context);
    }

    public interface ASMTransformer extends Transformer {
        default byte[] transform(byte[] input, String classname, BuildContext context) {
            ClassReader reader = new ClassReader(input);
            ClassNode cn = new ClassNode();
            reader.accept(cn, 0);
            transform(cn, classname, context);
            SafeClassWriter writer = new SafeClassWriter(context.task.reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            cn.accept(writer);
            return writer.toByteArray();
        }

        void transform(ClassNode cn, String classname, BuildContext context);
    }
}
