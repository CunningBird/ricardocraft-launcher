package ru.ricardocraft.client.runtime.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.profiles.ClientProfileVersions;
import ru.ricardocraft.client.profiles.PlayerProfile;
import ru.ricardocraft.client.profiles.optional.OptionalView;
import ru.ricardocraft.client.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.profiles.optional.actions.OptionalActionClassPath;
import ru.ricardocraft.client.profiles.optional.actions.OptionalActionClientArgs;
import ru.ricardocraft.client.profiles.optional.actions.OptionalActionJvmArgs;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.client.ClientParams;
import ru.ricardocraft.client.core.hasher.FileNameMatcher;
import ru.ricardocraft.client.core.hasher.HashedDir;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.serialize.HOutput;
import ru.ricardocraft.client.launch.RuntimeModuleManager;
import ru.ricardocraft.client.runtime.client.events.ClientProcessBuilderParamsWrittedEvent;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.utils.DirWatcher;
import ru.ricardocraft.client.utils.helper.*;
import ru.ricardocraft.client.utils.launch.*;

import javax.crypto.CipherOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientLauncherProcess {

    private transient final Logger logger = LoggerFactory.getLogger(ClientLauncherProcess.class);

    private final SettingsManager settingsManager;

    public final List<String> pre = new LinkedList<>();
    public final ClientParams params = new ClientParams();
    public final List<String> jvmArgs = new LinkedList<>();
    public final List<String> systemClassPath = new LinkedList<>();
    public final Map<String, String> systemEnv = new HashMap<>();
    public final String mainClass;
    private final transient Boolean[] waitWriteParams = new Boolean[]{false};
    private final RuntimeModuleManager modulesManager;
    public Path executeFile;
    public Path workDir;
    public JavaHelper.JavaVersion javaVersion;
    public boolean isStarted;

    private transient Process process;
    private final ClientProfile profile;

    public Process getProcess() {
        return process;
    }

    public ClientLauncherProcess(SettingsManager settingsManager,
                                 RuntimeModuleManager modulesManager,
                                 Path clientDir,
                                 Path assetDir,
                                 JavaHelper.JavaVersion javaVersion,
                                 Path resourcePackDir,
                                 ClientProfile profile,
                                 PlayerProfile playerProfile,
                                 OptionalView view,
                                 String accessToken,
                                 HashedDir clientHDir,
                                 HashedDir assetHDir,
                                 HashedDir jvmHDir) {
        this.settingsManager = settingsManager;
        this.modulesManager = modulesManager;
        this.javaVersion = javaVersion;
        this.workDir = clientDir.toAbsolutePath();
        this.executeFile = IOHelper.resolveJavaBin(this.javaVersion.jvmDir);
        this.profile = profile;
        this.mainClass = profile.getMainClass();
        this.params.clientDir = this.workDir.toString();
        this.params.resourcePackDir = resourcePackDir.toAbsolutePath().toString();
        this.params.assetDir = assetDir.toAbsolutePath().toString();
        this.params.timestamp = System.currentTimeMillis();
        Path nativesPath;
        if (profile.hasFlag(ClientProfile.CompatibilityFlags.LEGACY_NATIVES_DIR)) {
            nativesPath = workDir.resolve("natives");
        } else {
            nativesPath = workDir.resolve("natives").resolve(JVMHelper.OS_TYPE.name).resolve(javaVersion.arch.name);
        }
        if (!Files.isDirectory(nativesPath)) {
            throw new RuntimeException(String.format("Natives dir %s not exist! Your operating system or architecture not supported", nativesPath.toAbsolutePath()));
        }
        this.params.nativesDir = nativesPath.toString();
        this.params.profile = profile;
        this.params.playerProfile = playerProfile;
        this.params.accessToken = accessToken;
        this.params.assetHDir = assetHDir;
        this.params.clientHDir = clientHDir;
        this.params.javaHDir = jvmHDir;
        if (view != null) {
            this.params.actions = view.getEnabledActions();
        }

        this.systemClassPath.add(IOHelper.getCodeSource(JavaFXApplication.class).toAbsolutePath().toString());
        this.jvmArgs.addAll(this.params.profile.getJvmArgs());
        for (OptionalAction a : this.params.actions) {
            if (a instanceof OptionalActionJvmArgs) {
                this.jvmArgs.addAll(((OptionalActionJvmArgs) a).args);
            }
        }
        this.systemEnv.put("JAVA_HOME", javaVersion.jvmDir.toString());
        this.systemClassPath.addAll(this.params.profile.getAlternativeClassPath());
        if (params.ram > 0) {
            this.jvmArgs.add("-Xmx" + params.ram + 'M');
        }
        this.params.oauth = Request.getOAuth();
        if (this.params.oauth == null) {
            throw new UnsupportedOperationException("Legacy session not supported");
        } else {
            this.params.authId = Request.getAuthId();
            this.params.oauthExpiredTime = Request.getTokenExpiredTime();
            this.params.extendedTokens = Request.getExtendedTokens();
        }
    }

    public void start(boolean pipeOutput) throws Throwable {
        if (isStarted) throw new IllegalStateException("Process already started");

        logger.debug("Verifying ClientLauncher sign and classpath");
        Set<Path> ignoredPath = new HashSet<>();

        Path clientDir = Paths.get(params.clientDir);
        Path assetDir = Paths.get(params.assetDir);

        Launch launch;
        ClassLoaderControl classLoaderControl;
        List<Path> classpath = resolveClassPath(ignoredPath, clientDir, params.actions, params.profile)
                .collect(Collectors.toCollection(ArrayList::new));

        LaunchOptions options = new LaunchOptions();
        options.enableHacks = profile.hasFlag(ClientProfile.CompatibilityFlags.ENABLE_HACKS);
        options.moduleConf = profile.getModuleConf();

        ClientProfile.ClassLoaderConfig classLoaderConfig = profile.getClassLoaderConfig();
        if (classLoaderConfig == ClientProfile.ClassLoaderConfig.LAUNCHER || classLoaderConfig == ClientProfile.ClassLoaderConfig.MODULE) {
            if (JVMHelper.JVM_VERSION <= 11) {
                launch = new LegacyLaunch();
            } else {
                launch = new ModuleLaunch();
            }
            classLoaderControl = launch.init(classpath, params.nativesDir, options);
            System.setProperty("java.class.path", classpath.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)));
        } else if (classLoaderConfig == ClientProfile.ClassLoaderConfig.SYSTEM_ARGS) {
            launch = new BasicLaunch();
            classLoaderControl = launch.init(classpath, params.nativesDir, options);
        } else {
            throw new UnsupportedOperationException(String.format("Unknown classLoaderConfig %s", classLoaderConfig));
        }

        FileNameMatcher assetMatcher = profile.getAssetUpdateMatcher();
        FileNameMatcher clientMatcher = profile.getClientUpdateMatcher();
        Path javaDir = Paths.get(System.getProperty("java.home"));

        try (DirWatcher assetWatcher = new DirWatcher(settingsManager, assetDir, params.assetHDir, assetMatcher, true);
             DirWatcher clientWatcher = new DirWatcher(settingsManager, clientDir, params.clientHDir, clientMatcher, true);
             DirWatcher javaWatcher = params.javaHDir == null ? null : new DirWatcher(settingsManager, javaDir, params.javaHDir, null, true)) {

            CommonHelper.newThread("Asset Directory Watcher", true, assetWatcher).start();
            CommonHelper.newThread("Client Directory Watcher", true, clientWatcher).start();
            if (javaWatcher != null)
                CommonHelper.newThread("Java Directory Watcher", true, javaWatcher).start();
            verifyHDir(assetDir, params.assetHDir, assetMatcher, false, false);
            verifyHDir(clientDir, params.clientHDir, clientMatcher, false, true);
            if (javaWatcher != null)
                verifyHDir(javaDir, params.javaHDir, null, false, true);

            Collection<String> args = new LinkedList<>();
            if (profile.getVersion().compareTo(ClientProfileVersions.MINECRAFT_1_6_4) >= 0)
                params.addClientArgs(args);
            else {
                params.addClientLegacyArgs(args);
                System.setProperty("minecraft.applet.TargetDirectory", params.clientDir);
            }
            args.addAll(profile.getClientArgs());
            for (OptionalAction action : params.actions) {
                if (action instanceof OptionalActionClientArgs) {
                    args.addAll(((OptionalActionClientArgs) action).args);
                }
            }

            try {
                List<String> compatClasses = profile.getCompatClasses();
                for (String e : compatClasses) {
                    Class<?> clazz = classLoaderControl.getClass(e);
                    MethodHandle runMethod = MethodHandles.lookup().findStatic(clazz, "run", MethodType.methodType(void.class, ClassLoaderControl.class));
                    runMethod.invoke(classLoaderControl);
                }

                Launcher.LAUNCHED.set(true);
                JVMHelper.fullGC();
                launch.launch(params.profile.getMainClass(), params.profile.getMainModule(), args);
                logger.debug("Main exit successful");
            } catch (Throwable e) {
                logger.error(e.getMessage());
                throw e;
            } finally {
                settingsManager.exitLauncher(0);
            }
        }

//        try (DirWatcher assetWatcher = new DirWatcher(settingsManager, assetDir, params.assetHDir, assetMatcher, true);
//             DirWatcher clientWatcher = new DirWatcher(settingsManager, clientDir, params.clientHDir, clientMatcher, true);
//             DirWatcher javaWatcher = params.javaHDir == null ? null : new DirWatcher(settingsManager, javaDir, params.javaHDir, null, true)) {
//
//            CommonHelper.newThread("Asset Directory Watcher", true, assetWatcher).start();
//            CommonHelper.newThread("Client Directory Watcher", true, clientWatcher).start();
//            if (javaWatcher != null)
//                CommonHelper.newThread("Java Directory Watcher", true, javaWatcher).start();
//            verifyHDir(assetDir, params.assetHDir, assetMatcher, false, false);
//            verifyHDir(clientDir, params.clientHDir, clientMatcher, false, true);
//            if (javaWatcher != null)
//                verifyHDir(javaDir, params.javaHDir, null, false, true);
//
//            try {
//                List<String> compatClasses = profile.getCompatClasses();
//                for (String e : compatClasses) {
//                    Class<?> clazz = classLoaderControl.getClass(e);
//                    MethodHandle runMethod = MethodHandles.lookup().findStatic(clazz, "run", MethodType.methodType(void.class, ClassLoaderControl.class));
//                    runMethod.invoke(classLoaderControl);
//                }
//
//                ProcessBuilder minecraftProcess = launch.getLaunchProcess(getProcessArgs(), params, profile);
//
//                if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
//                    var env = minecraftProcess.environment();
//                    // https://github.com/Admicos/minecraft-wayland/issues/55
//                    env.put("__GL_THREADED_OPTIMIZATIONS", "0");
//                    if (params.lwjglGlfwWayland && !params.profile.hasFlag(ClientProfile.CompatibilityFlags.WAYLAND_USE_CUSTOM_GLFW)) {
//                        env.remove("DISPLAY"); // No X11
//                    }
//                }
//
//                minecraftProcess.environment().put("JAVA_HOME", javaVersion.jvmDir.toAbsolutePath().toString());
//                minecraftProcess.environment().putAll(systemEnv);
//                minecraftProcess.directory(workDir.toFile());
//                minecraftProcess.inheritIO();
//                if (pipeOutput) {
//                    minecraftProcess.redirectErrorStream(true);
//                    minecraftProcess.redirectOutput(ProcessBuilder.Redirect.PIPE);
//                }
//
//                EnvHelper.addEnv(minecraftProcess);
//
//                Launcher.LAUNCHED.set(true);
//                JVMHelper.fullGC();
//
//                process = minecraftProcess.start();
//                logger.debug("Main exit successful");
//            } catch (Throwable e) {
//                logger.error(e.getMessage());
//                throw e;
//            } finally {
//                settingsManager.exitLauncher(0);
//            }
//        }

        isStarted = true;
    }

    public void runWriteParams(SocketAddress address) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(address);
            synchronized (waitWriteParams) {
                waitWriteParams[0] = true;
                waitWriteParams.notifyAll();
            }
            Socket socket = serverSocket.accept();
            try (HOutput output = new HOutput(new CipherOutputStream(socket.getOutputStream(), SecurityHelper.newAESEncryptCipher(SecurityHelper.fromHex(Launcher.getConfig().secretKeyClient))))) {
                byte[] serializedMainParams = IOHelper.encode(Launcher.gsonManager.gson.toJson(params));
                output.writeByteArray(serializedMainParams, 0);
                params.clientHDir.write(output);
                params.assetHDir.write(output);
                if (params.javaHDir == null || params.javaHDir == params.assetHDir) { //OLD RUNTIME USE params.assetHDir AS NULL IN java.javaHDir
                    output.writeBoolean(false);
                } else {
                    output.writeBoolean(true);
                    params.javaHDir.write(output);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        modulesManager.invokeEvent(new ClientProcessBuilderParamsWrittedEvent(this));
    }

    public void verifyHDir(Path dir, HashedDir hdir, FileNameMatcher matcher, boolean digest, boolean checkExtra) throws IOException {
        HashedDir currentHDir = new HashedDir(dir, matcher, true, digest);
        HashedDir.Diff diff = hdir.diff(currentHDir, matcher);
        AtomicReference<String> latestPath = new AtomicReference<>("unknown");
        if (!diff.mismatch.isEmpty() || (checkExtra && !diff.extra.isEmpty())) {
            diff.extra.walk(File.separator, (e, k, v) -> {
                if (v.getType().equals(HashedEntry.Type.FILE)) {
                    logger.error("Extra file {}", e);
                    latestPath.set(e);
                } else logger.error("Extra {}", e);
                return HashedDir.WalkAction.CONTINUE;
            });
            diff.mismatch.walk(File.separator, (e, k, v) -> {
                if (v.getType().equals(HashedEntry.Type.FILE)) {
                    logger.error("Mismatch file {}", e);
                    latestPath.set(e);
                } else logger.error("Mismatch {}", e);
                return HashedDir.WalkAction.CONTINUE;
            });
            throw new SecurityException(String.format("Forbidden modification: '%s' file '%s'", IOHelper.getFileName(dir), latestPath.get()));
        }
    }

    private List<String> getProcessArgs() throws IOException, InterruptedException {
        List<String> processArgs = new LinkedList<>(pre);
        processArgs.add(executeFile.toString());
        processArgs.addAll(jvmArgs);
        //ADD CLASSPATH
        processArgs.add(JVMHelper.jvmProperty("java.library.path", this.params.nativesDir));
        if (params.profile.getClassLoaderConfig() == ClientProfile.ClassLoaderConfig.SYSTEM_ARGS) {
            Set<Path> ignorePath = new HashSet<>();
            var moduleConf = params.profile.getModuleConf();
            if (moduleConf != null) {
                if (moduleConf.modulePath != null && !moduleConf.modulePath.isEmpty()) {
                    processArgs.add("-p");
                    for (var e : moduleConf.modulePath) {
                        ignorePath.add(Path.of(e));
                    }
                    processArgs.add(String.join(File.pathSeparator, moduleConf.modulePath));
                }
                if (moduleConf.modules != null && !moduleConf.modules.isEmpty()) {
                    processArgs.add("--add-modules");
                    processArgs.add(String.join(",", moduleConf.modules));
                }
                if (moduleConf.exports != null && !moduleConf.exports.isEmpty()) {
                    for (var e : moduleConf.exports.entrySet()) {
                        processArgs.add("--add-exports");
                        processArgs.add(String.format("%s=%s", e.getKey(), e.getValue()));
                    }
                }
                if (moduleConf.opens != null && !moduleConf.opens.isEmpty()) {
                    for (var e : moduleConf.opens.entrySet()) {
                        processArgs.add("--add-opens");
                        processArgs.add(String.format("%s=%s", e.getKey(), e.getValue()));
                    }
                }
                if (moduleConf.reads != null && !moduleConf.reads.isEmpty()) {
                    for (var e : moduleConf.reads.entrySet()) {
                        processArgs.add("--add-reads");
                        processArgs.add(String.format("%s=%s", e.getKey(), e.getValue()));
                    }
                }
            }
            systemClassPath.addAll(resolveClassPath(ignorePath, workDir, params.actions, params.profile)
                    .map(Path::toString)
                    .toList());
        }

        synchronized (waitWriteParams) {
            if (!waitWriteParams[0]) {
                waitWriteParams.wait(1000);
            }
        }
        return processArgs;
    }

    private static Stream<Path> resolveClassPath(Set<Path> ignorePaths, Path clientDir, Set<OptionalAction> actions, ClientProfile profile) throws IOException {
        Stream<Path> result = resolveClassPathStream(ignorePaths, clientDir, profile.getClassPath());
        for (OptionalAction a : actions) {
            if (a instanceof OptionalActionClassPath)
                result = Stream.concat(result, resolveClassPathStream(ignorePaths, clientDir, ((OptionalActionClassPath) a).args));
        }
        return result;
    }

    private static Stream<Path> resolveClassPathStream(Set<Path> ignorePaths, Path clientDir, List<String> classPath) throws IOException {
        Stream.Builder<Path> builder = Stream.builder();
        for (String classPathEntry : classPath) {
            Path path = clientDir.resolve(IOHelper.toPath(classPathEntry.replace(IOHelper.CROSS_SEPARATOR, IOHelper.PLATFORM_SEPARATOR)));
            if (IOHelper.isDir(path)) { // Recursive walking and adding
                List<Path> jars = new ArrayList<>(32);
                IOHelper.walk(path, new ClassPathFileVisitor(jars), false);
                Collections.sort(jars);
                for (var e : jars) {
                    if (ignorePaths.contains(e)) {
                        continue;
                    }
                    builder.accept(e);
                    ignorePaths.add(e);
                }
                continue;
            }
            if (ignorePaths.contains(path)) {
                continue;
            }
            builder.accept(path);
            ignorePaths.add(path);
        }
        return builder.build();
    }

    private static final class ClassPathFileVisitor extends SimpleFileVisitor<Path> {
        private final List<Path> result;

        private ClassPathFileVisitor(List<Path> result) {
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (IOHelper.hasExtension(file, "jar") || IOHelper.hasExtension(file, "zip")) {
                result.add(file);
            }
            return super.visitFile(file, attrs);
        }
    }
}
