package ru.ricardocraft.client.runtime.client;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.LauncherConfig;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.PlayerProfile;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalActionClassPath;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalActionJvmArgs;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.client.ClientParams;
import ru.ricardocraft.client.core.hasher.HashedDir;
import ru.ricardocraft.client.core.serialize.HOutput;
import ru.ricardocraft.client.runtime.client.events.ClientProcessBuilderCreateEvent;
import ru.ricardocraft.client.runtime.client.events.ClientProcessBuilderLaunchedEvent;
import ru.ricardocraft.client.runtime.client.events.ClientProcessBuilderParamsWrittedEvent;
import ru.ricardocraft.client.runtime.client.events.ClientProcessBuilderPreLaunchEvent;
import ru.ricardocraft.client.utils.helper.*;

import javax.crypto.CipherOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class ClientLauncherProcess {
    public final List<String> pre = new LinkedList<>();
    public final ClientParams params = new ClientParams();
    public final List<String> jvmArgs = new LinkedList<>();
    public final List<String> jvmModules = new LinkedList<>();
    public final List<String> jvmModulesPaths = new LinkedList<>();
    public final List<String> systemClientArgs = new LinkedList<>();
    public final List<String> systemClassPath = new LinkedList<>();
    public final Map<String, String> systemEnv = new HashMap<>();
    public final String mainClass;
    private final transient Boolean[] waitWriteParams = new Boolean[]{false};
    public Path executeFile;
    public Path workDir;
    public JavaHelper.JavaVersion javaVersion;
    public boolean useLegacyJavaClassPathProperty;
    public boolean isStarted;
    private transient Process process;

    public ClientLauncherProcess(Path clientDir, Path assetDir, JavaHelper.JavaVersion javaVersion, Path resourcePackDir,
                                 ClientProfile profile, PlayerProfile playerProfile, OptionalView view, String accessToken,
                                 HashedDir clientHDir, HashedDir assetHDir, HashedDir jvmHDir) {
        this.javaVersion = javaVersion;
        this.workDir = clientDir.toAbsolutePath();
        this.executeFile = IOHelper.resolveJavaBin(this.javaVersion.jvmDir);
        this.mainClass = JavaFXApplication.class.getName();
        this.params.clientDir = this.workDir.toString();
        this.params.resourcePackDir = resourcePackDir.toAbsolutePath().toString();
        this.params.assetDir = assetDir.toAbsolutePath().toString();
        this.params.timestamp = System.currentTimeMillis();
        Path nativesPath;
        if(profile.hasFlag(ClientProfile.CompatibilityFlags.LEGACY_NATIVES_DIR)) {
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
        applyClientProfile();
    }

    public static String getPathSeparator() {
        if (JVMHelper.OS_TYPE == JVMHelper.OS.WINDOWS)
            return ";";
        else
            return ":";
    }

    private void applyClientProfile() {
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
        JavaFXApplication.modulesManager.invokeEvent(new ClientProcessBuilderCreateEvent(this));
    }

    public void start(boolean pipeOutput) throws IOException, InterruptedException {
        if (isStarted) throw new IllegalStateException("Process already started");
        JavaFXApplication.modulesManager.invokeEvent(new ClientProcessBuilderPreLaunchEvent(this));
        List<String> processArgs = new LinkedList<>(pre);
        processArgs.add(executeFile.toString());
        processArgs.addAll(jvmArgs);
        if (javaVersion.version >= 9) {
            applyJava9Params(processArgs);
        }
        //ADD CLASSPATH
        processArgs.add(JVMHelper.jvmProperty("java.library.path", this.params.nativesDir));
        if (params.profile.getClassLoaderConfig() == ClientProfile.ClassLoaderConfig.SYSTEM_ARGS) {
            Set<Path> ignorePath = new HashSet<>();
            var moduleConf = params.profile.getModuleConf();
            if(moduleConf != null) {
                if(moduleConf.modulePath != null && !moduleConf.modulePath.isEmpty()) {
                    processArgs.add("-p");
                    for(var e : moduleConf.modulePath) {
                        ignorePath.add(Path.of(e));
                    }
                    processArgs.add(String.join(File.pathSeparator, moduleConf.modulePath));
                }
                if(moduleConf.modules != null && !moduleConf.modules.isEmpty()) {
                    processArgs.add("--add-modules");
                    processArgs.add(String.join(",", moduleConf.modules));
                }
                if(moduleConf.exports != null && !moduleConf.exports.isEmpty()) {
                    for(var e : moduleConf.exports.entrySet()) {
                        processArgs.add("--add-exports");
                        processArgs.add(String.format("%s=%s", e.getKey(), e.getValue()));
                    }
                }
                if(moduleConf.opens != null && !moduleConf.opens.isEmpty()) {
                    for(var e : moduleConf.opens.entrySet()) {
                        processArgs.add("--add-opens");
                        processArgs.add(String.format("%s=%s", e.getKey(), e.getValue()));
                    }
                }
                if(moduleConf.reads != null && !moduleConf.reads.isEmpty()) {
                    for(var e : moduleConf.reads.entrySet()) {
                        processArgs.add("--add-reads");
                        processArgs.add(String.format("%s=%s", e.getKey(), e.getValue()));
                    }
                }
            }
            systemClassPath.addAll(resolveClassPath(ignorePath, workDir, params.actions, params.profile)
                    .map(Path::toString)
                    .toList());
        }
        if (Launcher.getConfig().environment != LauncherConfig.LauncherEnvironment.PROD) {
            processArgs.add(JVMHelper.jvmProperty(LogHelper.DEV_PROPERTY, String.valueOf(LogHelper.isDevEnabled())));
            processArgs.add(JVMHelper.jvmProperty(LogHelper.DEBUG_PROPERTY, String.valueOf(LogHelper.isDebugEnabled())));
            processArgs.add(JVMHelper.jvmProperty(LogHelper.STACKTRACE_PROPERTY, String.valueOf(LogHelper.isStacktraceEnabled())));
        }
        if (useLegacyJavaClassPathProperty) {
            processArgs.add("-Djava.class.path=".concat(String.join(getPathSeparator(), systemClassPath)));
        } else {
            processArgs.add("-cp");
            processArgs.add(String.join(getPathSeparator(), systemClassPath));
        }
        processArgs.add(mainClass);
        processArgs.addAll(systemClientArgs);
        synchronized (waitWriteParams) {
            if (!waitWriteParams[0]) {
                waitWriteParams.wait(1000);
            }
        }
        if (LogHelper.isDebugEnabled())
            LogHelper.debug("Commandline: %s", Arrays.toString(processArgs.toArray()));
        ProcessBuilder processBuilder = new ProcessBuilder(processArgs);
        EnvHelper.addEnv(processBuilder);
        if(JVMHelper.OS_TYPE == JVMHelper.OS.LINUX){
            var env = processBuilder.environment();
            // https://github.com/Admicos/minecraft-wayland/issues/55
            env.put("__GL_THREADED_OPTIMIZATIONS", "0");
            if(params.lwjglGlfwWayland && !params.profile.hasFlag(ClientProfile.CompatibilityFlags.WAYLAND_USE_CUSTOM_GLFW)) {
                env.remove("DISPLAY"); // No X11
            }
        }
        processBuilder.environment().put("JAVA_HOME", javaVersion.jvmDir.toAbsolutePath().toString());
        processBuilder.environment().putAll(systemEnv);
        processBuilder.directory(workDir.toFile());
        processBuilder.inheritIO();
        if (pipeOutput) {
            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        }
        process = processBuilder.start();
        JavaFXApplication.modulesManager.invokeEvent(new ClientProcessBuilderLaunchedEvent(this));
        isStarted = true;
    }

    private void applyJava9Params(List<String> processArgs) {
        // TODO: fix runtime in client
        StringBuilder modulesPath = new StringBuilder();
        StringBuilder modulesAdd = new StringBuilder();
        for (String moduleName : jvmModules) {
            if (!modulesAdd.isEmpty()) modulesAdd.append(",");
            modulesAdd.append(moduleName);
        }
        for (String modulePath : jvmModulesPaths) {
            if (!modulesPath.isEmpty()) modulesPath.append(File.pathSeparator);
            modulesPath.append(modulePath);
        }
        if (!modulesAdd.isEmpty()) {
            processArgs.add("--add-modules");
            processArgs.add(modulesAdd.toString());
        }
        if (!modulesPath.isEmpty()) {
            processArgs.add("--module-path");
            processArgs.add(modulesPath.toString());
        }
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
        JavaFXApplication.modulesManager.invokeEvent(new ClientProcessBuilderParamsWrittedEvent(this));
    }

    public Process getProcess() {
        return process;
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
