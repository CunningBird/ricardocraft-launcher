package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import ru.ricardocraft.backend.base.LauncherConfig;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.SecurityHelper;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.binary.JarLauncherInfo;
import ru.ricardocraft.backend.binary.tasks.main.BuildContext;
import ru.ricardocraft.backend.binary.tasks.main.ClassMetadataReader;
import ru.ricardocraft.backend.binary.tasks.main.InjectClassAcceptor;
import ru.ricardocraft.backend.binary.tasks.main.SafeClassWriter;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.DirectoriesProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

public class MainBuildTask implements LauncherBuildTask {

    private transient final Logger logger = LogManager.getLogger(MainBuildTask.class);

    private final ClassMetadataReader reader;
    private final Set<String> blacklist = new HashSet<>();
    private final List<Transformer> transformers = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();

    private final JarLauncherInfo jarLauncherInfo;
    private final JarLauncherBinary launcherBinary;
    private final LaunchServerProperties config;
    private final DirectoriesProperties directoriesProperties;
    private final DirectoriesManager directoriesManager;
    private final NettyProperties nettyProperties;
    private final KeyAgreementManager keyAgreementManager;
    private final CertificateManager certificateManager;

    public MainBuildTask(JarLauncherInfo jarLauncherInfo,
                         JarLauncherBinary launcherBinary,
                         LaunchServerProperties config,
                         DirectoriesProperties directoriesProperties,
                         DirectoriesManager directoriesManager,
                         NettyProperties nettyProperties,
                         KeyAgreementManager keyAgreementManager,
                         CertificateManager certificateManager) {
        this.jarLauncherInfo = jarLauncherInfo;
        this.launcherBinary = launcherBinary;
        this.config = config;
        this.directoriesProperties = directoriesProperties;
        this.directoriesManager = directoriesManager;
        this.nettyProperties = nettyProperties;
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
            BuildContext context = new BuildContext(output, reader.getCp(), this, directoriesManager.getRuntimeDir());
            initProps();
            properties.put("launcher.legacymodules", context.legacyClientModules.stream().map(e -> Type.getObjectType(e.replace('.', '/'))).collect(Collectors.toList()));
            properties.put("launcher.modules", context.clientModules.stream().map(e -> Type.getObjectType(e.replace('.', '/'))).collect(Collectors.toList()));
            postInitProps();
            reader.getCp().add(new JarFile(inputJar.toFile()));
            for (Path e : jarLauncherInfo.getCoreLibs()) {
                reader.getCp().add(new JarFile(e.toFile()));
            }
            context.pushJarFile(inputJar, (e) -> blacklist.contains(e.getName()) || e.getName().startsWith("pro/gravit/launcher/debug/"), (e) -> true);

            // map for guard
            Map<String, byte[]> runtime = new HashMap<>(256);
            // Write launcher guard dir
            if (config.getLauncher().getEncryptRuntime()) {
                context.pushEncryptedDir(context.getRuntimeDir(), directoriesManager.getRuntimeDir(), this.config.getRuntime().getRuntimeEncryptKey(), runtime, false);
            } else {
                context.pushDir(context.getRuntimeDir(), directoriesManager.getRuntimeDir(), runtime, false);
            }
            if (context.isDeleteRuntimeDir()) {
                IOHelper.deleteDir(context.getRuntimeDir(), true);
            }

            LauncherConfig launcherConfig = new LauncherConfig(nettyProperties.getAddress(), keyAgreementManager.ecdsaPublicKey, keyAgreementManager.rsaPublicKey, runtime, config.getProjectName());
            context.pushFile(directoriesProperties.getLauncherConfigFile(), launcherConfig);
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
        if (!config.getSign().getEnabled()) {
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
        properties.put("launcher.address", nettyProperties.getAddress());
        properties.put("launcher.projectName", config.getProjectName());
        properties.put("runtimeconfig.secretKeyClient", SecurityHelper.randomStringAESKey());
        properties.put("launcher.port", 32148 + SecurityHelper.newRandom().nextInt(512));
        properties.put("launchercore.env", config.getEnv());
        properties.put("launcher.memory", config.getLauncher().getMemoryLimit());
        properties.put("launcher.customJvmOptions", config.getLauncher().getCustomJvmOptions());
        properties.put("runtimeconfig.runtimeEncryptKey", config.getRuntime().getRuntimeEncryptKey());
        properties.put("launcher.certificatePinning", config.getLauncher().getCertificatePinning());
        properties.put("runtimeconfig.passwordEncryptKey", config.getRuntime().getPasswordEncryptKey());
        String launcherSalt = SecurityHelper.randomStringToken();
        byte[] launcherSecureHash = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
                config.getRuntime().getClientCheckSecret().concat(".").concat(launcherSalt));
        properties.put("runtimeconfig.secureCheckHash", Base64.getEncoder().encodeToString(launcherSecureHash));
        properties.put("runtimeconfig.secureCheckSalt", launcherSalt);
        properties.put("runtimeconfig.unlockSecret", config.getRuntime().getUnlockSecret());
        properties.put("runtimeconfig.buildNumber", config.getRuntime().getBuildNumber());
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
