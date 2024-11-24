package ru.ricardocraft.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.LauncherConfig;
import ru.ricardocraft.client.base.modules.JavaRuntimeModule;
import ru.ricardocraft.client.base.modules.events.OfflineModeEvent;
import ru.ricardocraft.client.base.modules.events.PreConfigPhase;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.RequestException;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.base.request.auth.AuthRequest;
import ru.ricardocraft.client.base.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.client.base.request.websockets.OfflineRequestService;
import ru.ricardocraft.client.base.request.websockets.StdWebSocketService;
import ru.ricardocraft.client.client.*;
import ru.ricardocraft.client.client.events.ClientExitPhase;
import ru.ricardocraft.client.commands.RuntimeCommand;
import ru.ricardocraft.client.commands.VersionCommand;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.config.StdSettingsManager;
import ru.ricardocraft.client.core.LauncherInject;
import ru.ricardocraft.client.core.LauncherTrustManager;
import ru.ricardocraft.client.helper.EnFSHelper;
import ru.ricardocraft.client.impl.*;
import ru.ricardocraft.client.launch.*;
import ru.ricardocraft.client.runtime.NewLauncherSettings;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.runtime.client.RuntimeGsonManager;
import ru.ricardocraft.client.runtime.client.UserSettings;
import ru.ricardocraft.client.runtime.console.GetPublicKeyCommand;
import ru.ricardocraft.client.runtime.console.ModulesCommand;
import ru.ricardocraft.client.runtime.console.SignDataCommand;
import ru.ricardocraft.client.runtime.managers.ConsoleManager;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.runtime.utils.LauncherUpdater;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.service.*;
import ru.ricardocraft.client.stage.PrimaryStage;
import ru.ricardocraft.client.utils.command.BaseCommandCategory;
import ru.ricardocraft.client.utils.command.CommandCategory;
import ru.ricardocraft.client.utils.command.CommandHandler;
import ru.ricardocraft.client.utils.helper.*;

import javax.swing.*;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class JavaFXApplication extends Application {

    private static long startTime;

    public static RuntimeModuleManager modulesManager = new RuntimeModuleManager();
    public static ECPublicKey publicKey;
    public static ECPrivateKey privateKey;

    private static final AtomicBoolean started = new AtomicBoolean(false);
    public static final AtomicBoolean IS_DEBUG = new AtomicBoolean(false);

    private static MethodHandles.Lookup hackLookup;
    public static ModuleLayer layer;
    public static boolean disablePackageDelegateSupport;

    public static final String MAGIC_ARG = "-Djdk.attach.allowAttachSelf";
    public static final String WAIT_PROCESS_PROPERTY = "launcher.waitProcess";
    public static final String NO_JAVA_CHECK_PROPERTY = "launcher.noJavaCheck";
    public static boolean noJavaCheck = Boolean.getBoolean(NO_JAVA_CHECK_PROPERTY);
    public static boolean waitProcess = Boolean.getBoolean(WAIT_PROCESS_PROPERTY);
    @LauncherInject("launcher.memory")
    public static int launcherMemoryLimit;
    @LauncherInject("launcher.customJvmOptions")
    public static List<String> customJvmOptions;

    public static volatile Path updatePath;
    private static final AtomicReference<JavaFXApplication> INSTANCE = new AtomicReference<>();
    private static Path runtimeDirectory = null;
    public final LauncherConfig config = Launcher.getConfig();
    public final ExecutorService workers = Executors.newWorkStealingPool(4);
    public RuntimeSettings runtimeSettings;
    public RequestService service;
    public GuiObjectsContainer gui;
    public AuthService authService;
    public ProfilesService profilesService;
    public LaunchService launchService;
    public GuiModuleConfig guiModuleConfig;
    public MessageManager messageManager;
    public RuntimeSecurityService securityService;
    public SkinManager skinManager;
    public FXMLFactory fxmlFactory;
    public JavaService javaService;
    public PingService pingService;
    public OfflineService offlineService;
    public TriggerManager triggerManager;
    private SettingsManager settingsManager;
    private PrimaryStage mainStage;
    private boolean debugMode;
    private ResourceBundle resources;
    private static Path enfsDirectory;

    public JavaFXApplication() {
        INSTANCE.set(this);
    }

    @Override
    public void init() throws Exception {
        LogHelper.printVersion("Launcher");
        LogHelper.printLicense("Launcher");
//        JVMHelper.checkStackTrace(JavaFXApplication.class);
        JVMHelper.verifySystemProperties(Launcher.class, true);
        EnvHelper.checkDangerousParams();
        LauncherConfig config = Launcher.getConfig();

//        IS_DEBUG.set(true);

        LauncherConfig.initModules(modulesManager);
        LogHelper.info("Launcher for project %s", config.projectName);
        if (config.environment.equals(LauncherConfig.LauncherEnvironment.PROD)) {
            if (System.getProperty(LogHelper.DEBUG_PROPERTY) != null) {
                LogHelper.warning("Found -Dlauncher.debug=true");
            }
            if (System.getProperty(LogHelper.STACKTRACE_PROPERTY) != null) {
                LogHelper.warning("Found -Dlauncher.stacktrace=true");
            }
            LogHelper.info("Debug mode disabled (found env PRODUCTION)");
        } else {
            LogHelper.info("If need debug output use -Dlauncher.debug=true");
            LogHelper.info("If need stacktrace output use -Dlauncher.stacktrace=true");
            if (LogHelper.isDebugEnabled()) waitProcess = true;
        }

        LogHelper.info("Restart Launcher with JavaAgent...");

        ClientLauncherWrapperContext context = new ClientLauncherWrapperContext();
        context.processBuilder = new ProcessBuilder();
        if (waitProcess) context.processBuilder.inheritIO();

        context.javaVersion = null;
        try {
            if (!noJavaCheck) {
                List<JavaHelper.JavaVersion> javaVersions = JavaHelper.findJava();
                for (JavaHelper.JavaVersion version : javaVersions) {
                    LogHelper.debug("Found Java %d b%d in %s javafx %s", version.version, version.build, version.jvmDir.toString(), version.enabledJavaFX ? "supported" : "not supported");
                    if (context.javaVersion == null) {
                        context.javaVersion = version;
                        continue;
                    }
                    if (version.enabledJavaFX && !context.javaVersion.enabledJavaFX) {
                        context.javaVersion = version;
                        continue;
                    }
                    if (version.enabledJavaFX == context.javaVersion.enabledJavaFX) {
                        if (context.javaVersion.version < version.version) {
                            context.javaVersion = version;
                        } else if (context.javaVersion.version == version.version && context.javaVersion.build < version.build) {
                            context.javaVersion = version;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LogHelper.error(e);
        }
        if (context.javaVersion == null) {
            context.javaVersion = JavaHelper.JavaVersion.getCurrentJavaVersion();
        }

        context.executePath = IOHelper.resolveJavaBin(context.javaVersion.jvmDir);
        context.jvmProperties.put(LogHelper.DEBUG_PROPERTY, Boolean.toString(LogHelper.isDebugEnabled()));
        context.jvmProperties.put(LogHelper.STACKTRACE_PROPERTY, Boolean.toString(LogHelper.isStacktraceEnabled()));
        context.jvmProperties.put(LogHelper.DEV_PROPERTY, Boolean.toString(LogHelper.isDevEnabled()));

        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        List<Path> classpathList = Stream.of(classpathEntries).map(Path::of).toList();

        EnvHelper.addEnv(context.processBuilder);
        modulesManager.callWrapper(context);
        // ---------

        // Args
        List<String> args = new ArrayList<>(16);
        args.add(context.executePath.toAbsolutePath().toString());
        args.add(MAGIC_ARG);
        args.add("-XX:+DisableAttachMechanism");
        context.jvmProperties.forEach((key, value) -> args.add(String.format("-D%s=%s", key, value)));
        if (launcherMemoryLimit != 0) args.add(String.format("-Xmx%dM", launcherMemoryLimit));
        if (customJvmOptions != null) args.addAll(customJvmOptions);
        args.add("-cp");
        args.add(classpath);
        args.add("ru.ricardocraft.client.LauncherEngineWrapper");
        LogHelper.debug("Commandline: " + args);

        // Modules
        List<String> jvmModules = List.of(
                "javafx.base",
                "javafx.graphics",
                "javafx.fxml",
                "javafx.controls",
                "javafx.media",
                "javafx.web"
        );

        // Options
        LaunchOptions options = new LaunchOptions();
        options.disablePackageDelegateSupport = true;
        options.moduleConf = new LaunchOptions.ModuleConf();
        var libDirectory = Path.of(System.getProperty("java.home")).resolve("lib");
        for (var moduleName : jvmModules) {
            var path = libDirectory.resolve(moduleName.concat(".jar"));
            if (Files.exists(path)) {
                options.moduleConf.modules.add(moduleName);
                options.moduleConf.modulePath.add(path.toAbsolutePath().toString());
            }
        }

        init(classpathList, options);
        String[] startArguments = args.toArray(new String[0]);

//        JVMHelper.checkStackTrace(JavaFXApplication.class);
        JVMHelper.verifySystemProperties(Launcher.class, false);
        checkClass(JavaFXApplication.class.getClassLoader().getClass());
        EnvHelper.checkDangerousParams();
        verifyNoAgent();
        if (contains(startArguments, "--log-output") && Launcher.getConfig().environment != LauncherConfig.LauncherEnvironment.PROD) {
            LogHelper.addOutput(Paths.get("Launcher.log"));
        }
        LogHelper.printVersion("Launcher");
        LogHelper.printLicense("Launcher");
        modulesManager.loadModule(new RuntimeLauncherCoreModule());
        LauncherConfig.initModules(modulesManager);
        modulesManager.initModules(null);
        // Start Launcher
        initGson(modulesManager);
        ConsoleManager.initConsole();
        modulesManager.invokeEvent(new PreConfigPhase());

        startTime = System.currentTimeMillis();

        preGuiPhase();
        if (!Request.isAvailable()) {
            String address = Launcher.getConfig().address;
            LogHelper.debug("Start async connection to %s", address);
            RequestService service;
            try {
                service = StdWebSocketService.initWebSockets(address).get();
            } catch (Throwable e) {
                if (LogHelper.isDebugEnabled()) {
                    LogHelper.error(e);
                }
                LogHelper.warning("Launcher in offline mode");
                service = initOffline();
            }
            Request.setRequestService(service);
            if (service instanceof StdWebSocketService) {
                ((StdWebSocketService) service).reconnectCallback = () ->
                {
                    LogHelper.debug("WebSocket connect closed. Try reconnect");
                    try {
                        Request.reconnect();
                    } catch (Exception e) {
                        LogHelper.error(e);
                        throw new RequestException("Connection failed", e);
                    }
                };
            }
        }
        Request.startAutoRefresh();
        Request.getRequestService().registerEventHandler(new BasicLauncherEventHandler());
        Objects.requireNonNull(args, "args");
        if (started.getAndSet(true)) throw new IllegalStateException("Launcher has been already started");
        readKeys();
        registerCommandsApp();
        LogHelper.debug("Dir: %s", DirBridge.dir);
        LogHelper.debug("Start JavaFX Application");


        guiModuleConfig = new GuiModuleConfig();
        settingsManager = new StdSettingsManager();
        UserSettings.providers.register(JavaRuntimeModule.RUNTIME_NAME, RuntimeSettings.class);
        settingsManager.loadConfig();
        NewLauncherSettings settings = settingsManager.getConfig();
        if (settings.userSettings.get(JavaRuntimeModule.RUNTIME_NAME) == null)
            settings.userSettings.put(JavaRuntimeModule.RUNTIME_NAME, RuntimeSettings.getDefault(guiModuleConfig));
        runtimeSettings = (RuntimeSettings) settings.userSettings.get(JavaRuntimeModule.RUNTIME_NAME);
        runtimeSettings.apply();
        System.setProperty("prism.vsync", String.valueOf(runtimeSettings.globalSettings.prismVSync));
        DirBridge.dirUpdates = runtimeSettings.updatesDir == null
                ? DirBridge.defaultUpdatesDir
                : runtimeSettings.updatesDir;
        service = Request.getRequestService();
        service.registerEventHandler(new GuiEventHandler(this));
        authService = new AuthService(this);
        launchService = new LaunchService(this);
        profilesService = new ProfilesService(this);
        messageManager = new MessageManager(this);
        securityService = new RuntimeSecurityService(this);
        skinManager = new SkinManager(this);
        triggerManager = new TriggerManager(this);
        javaService = new JavaService(this);
        offlineService = new OfflineService(this);
        pingService = new PingService();
        registerCommands();
    }

    @Override
    public void start(Stage stage) {
        // If debugging
        try {
            runtimeDirectory = IOHelper.WORKING_DIR.resolve("runtime");
            if (JavaFXApplication.IS_DEBUG.get()) {
                debugMode = true;
            }
        } catch (Throwable e) {
            LogHelper.error(e);
        }

        try {
            EnFSHelper.initEnFS();
            String themeDir = runtimeSettings.theme == null ? RuntimeSettings.LAUNCHER_THEME.COMMON.name : runtimeSettings.theme.name;
            enfsDirectory = EnFSHelper.initEnFSDirectory(config, themeDir, runtimeDirectory);
        } catch (Throwable e) {
            LogHelper.error(e);
            if (config.runtimeEncryptKey != null) {
                JavaRuntimeModule.noEnFSAlert();
            }
        }

        // System loading
        if (runtimeSettings.locale == null) runtimeSettings.locale = RuntimeSettings.DEFAULT_LOCALE;

        try {
            updateLocaleResources(runtimeSettings.locale.name);
        } catch (Throwable e) {
            JavaRuntimeModule.noLocaleAlert(runtimeSettings.locale.name);
            if (!(e instanceof FileNotFoundException)) {
                LogHelper.error(e);
            }
            Platform.exit();
        }

        RuntimeDialogService dialogService = new RuntimeDialogService(messageManager);
        DialogService.setNotificationImpl(dialogService);

        if (offlineService.isOfflineMode()) {
            if (!offlineService.isAvailableOfflineMode() && !debugMode) {
                messageManager.showDialog(getTranslation("runtime.offline.dialog.header"),
                        getTranslation("runtime.offline.dialog.text"),
                        Platform::exit, Platform::exit, false);
                return;
            }
        }
        try {
            mainStage = new PrimaryStage(this, stage, "%s Launcher".formatted(config.projectName));
            // Overlay loading
            gui = new GuiObjectsContainer(this);
            gui.init();
            //
            mainStage.setScene(gui.loginScene, true);
            gui.background.init();
            mainStage.pushBackground(gui.background);
            mainStage.show();
            if (offlineService.isOfflineMode()) {
                messageManager.createNotification(getTranslation("runtime.offline.notification.header"),
                        getTranslation("runtime.offline.notification.text"));
            }

            AuthRequest.registerProviders();
        } catch (Throwable e) {
            LogHelper.error(e);
            JavaRuntimeModule.errorHandleAlert(e);
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        LogHelper.debug("JavaFX method stop invoked");
        modulesManager.invokeEvent(new ClientExitPhase(0));

        LogHelper.debug("Post Application.launch method invoked");
        if (updatePath != null) {
            beforeExit(0);
            Path target = IOHelper.getCodeSource(LauncherUpdater.class);
            try {
                try (InputStream input = IOHelper.newInput(updatePath)) {
                    try (OutputStream output = IOHelper.newOutput(target)) {
                        IOHelper.transfer(input, output);
                    }
                }
                Files.deleteIfExists(updatePath);
            } catch (IOException e) {
                LogHelper.error(e);
                forceExit(-109);
            }
            LauncherUpdater.restart();
        }

        long endTime = System.currentTimeMillis();
        LogHelper.debug("Launcher started in %dms", endTime - startTime);

        exitLauncher(0);
    }

    public static JavaFXApplication getInstance() {
        return INSTANCE.get();
    }

    public static URL getResourceURL(String name) throws IOException {
        if (enfsDirectory != null) {
            return getResourceEnFs(name);
        } else if (runtimeDirectory != null) {
            Path target = runtimeDirectory.resolve(name);
            if (!Files.exists(target)) throw new FileNotFoundException("File runtime/%s not found".formatted(name));
            return target.toUri().toURL();
        } else {
            return Launcher.getResourceURL(name);
        }
    }

    public AbstractScene getCurrentScene() {
        return (AbstractScene) mainStage.getVisualComponent();
    }

    public PrimaryStage getMainStage() {
        return mainStage;
    }

    public void updateLocaleResources(String locale) throws IOException {
        try (InputStream input = getResource("runtime_%s.properties".formatted(locale))) {
            resources = new PropertyResourceBundle(input);
        }
        fxmlFactory = new FXMLFactory(resources, workers);
    }

    public void resetDirectory() throws IOException {
        if (enfsDirectory != null) {
            String themeDir = runtimeSettings.theme == null ? RuntimeSettings.LAUNCHER_THEME.COMMON.name :
                    runtimeSettings.theme.name;
            enfsDirectory = EnFSHelper.initEnFSDirectory(config, themeDir, runtimeDirectory);
        }
    }

    public void registerPrivateCommands() {
        if (runtimeCategory == null) return;
        runtimeCategory.registerCommand("runtime", new RuntimeCommand(this));
    }

    public boolean isThemeSupport() {
        return enfsDirectory != null;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public URL tryResource(String name) {
        try {
            return getResourceURL(name);
        } catch (IOException e) {
            return null;
        }

    }

    public RuntimeSettings.ProfileSettings getProfileSettings() {
        return getProfileSettings(profilesService.getProfile());
    }

    public RuntimeSettings.ProfileSettings getProfileSettings(ClientProfile profile) {
        if (profile == null) throw new NullPointerException("ClientProfile not selected");
        UUID uuid = profile.getUUID();
        RuntimeSettings.ProfileSettings settings = runtimeSettings.profileSettings.get(uuid);
        if (settings == null) {
            settings = RuntimeSettings.ProfileSettings.getDefault(javaService, profile);
            runtimeSettings.profileSettings.put(uuid, settings);
        }
        return settings;
    }

    public void setMainScene(AbstractScene scene) throws Exception {
        mainStage.setScene(scene, true);
    }

    public Stage newStage() {
        return newStage(StageStyle.TRANSPARENT);
    }

    public Stage newStage(StageStyle style) {
        Stage ret = new Stage();
        ret.initStyle(style);
        ret.setResizable(false);
        return ret;
    }

    public final String getTranslation(String name) {
        return getTranslation(name, "'%s'".formatted(name));
    }

    public final String getTranslation(String key, String defaultValue) {
        try {
            return resources.getString(key);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public void openURL(String url) {
        try {
            getHostServices().showDocument(url);
        } catch (Throwable e) {
            LogHelper.error(e);
        }
    }

    public void saveSettings() throws IOException {
        settingsManager.saveConfig();
        if (profilesService != null) {
            try {
                profilesService.saveAll();
            } catch (Throwable ex) {
                LogHelper.error(ex);
            }
        }
    }

    private InputStream getResource(String name) throws IOException {
        return IOHelper.newInput(getResourceURL(name));
    }

    private CommandCategory runtimeCategory;

    private void registerCommands() {
        runtimeCategory = new BaseCommandCategory();
        runtimeCategory.registerCommand("version", new VersionCommand());
        if (ConsoleManager.isConsoleUnlock) {
            registerPrivateCommands();
        }
        ConsoleManager.handler.registerCategory(new CommandHandler.Category(runtimeCategory, "runtime"));
    }

    private static URL getResourceEnFs(String name) throws IOException {
        return EnFSHelper.getURL(enfsDirectory.resolve(name).toString().replaceAll("\\\\", "/"));
        //return EnFS.main.getURL(enfsDirectory.resolve(name));
    }

    private static void init(List<Path> files, LaunchOptions options) {
        disablePackageDelegateSupport = options.disablePackageDelegateSupport;

        ModuleClassLoader moduleClassLoader = new ModuleClassLoader(files.stream().map((e) -> {
            try {
                return e.toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }).toArray(URL[]::new), ClassLoader.getPlatformClassLoader());

        moduleClassLoader.nativePath = null;
        if (options.enableHacks) {
            hackLookup = HackHelper.createHackLookup(JavaFXApplication.class);
        }
        if (options.moduleConf != null) {
            // Create Module Layer
            ModuleFinder moduleFinder = ModuleFinder.of(options.moduleConf.modulePath.stream().map(Paths::get).map(Path::toAbsolutePath).toArray(Path[]::new));
            ModuleLayer bootLayer = ModuleLayer.boot();
            if (options.moduleConf.modules.contains("ALL-MODULE-PATH")) {
                var set = moduleFinder.findAll();
                if (LogHelper.isDevEnabled()) {
                    for (var m : set) {
                        LogHelper.dev("Found module %s in %s", m.descriptor().name(), m.location().map(URI::toString).orElse("unknown"));
                    }
                    LogHelper.dev("Found %d modules", set.size());
                }
                for (var m : set) {
                    options.moduleConf.modules.add(m.descriptor().name());
                }
                options.moduleConf.modules.remove("ALL-MODULE-PATH");
            }
            Configuration configuration = bootLayer.configuration()
                    .resolveAndBind(moduleFinder, ModuleFinder.of(), options.moduleConf.modules);
            ModuleLayer.Controller controller = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(bootLayer), moduleClassLoader);
            layer = controller.layer();
            // Configure exports / opens
            for (var e : options.moduleConf.exports.entrySet()) {
                String[] split = e.getKey().split("/");
                String moduleName = split[0];
                String pkg = split[1];
                LogHelper.dev("Export module: %s package: %s to %s", moduleName, pkg, e.getValue());
                Module source = layer.findModule(split[0]).orElse(null);
                if (source == null) {
                    throw new RuntimeException(String.format("Module %s not found", moduleName));
                }
                Module target = layer.findModule(e.getValue()).orElse(null);
                if (target == null) {
                    throw new RuntimeException(String.format("Module %s not found", e.getValue()));
                }
                if (options.enableHacks && source.getLayer() != layer) {
                    ModuleHacks.createController(hackLookup, source.getLayer()).addExports(source, pkg, target);
                } else {
                    controller.addExports(source, pkg, target);
                }
            }
            for (var e : options.moduleConf.opens.entrySet()) {
                String[] split = e.getKey().split("/");
                String moduleName = split[0];
                String pkg = split[1];
                LogHelper.dev("Open module: %s package: %s to %s", moduleName, pkg, e.getValue());
                Module source = layer.findModule(split[0]).orElse(null);
                if (source == null) {
                    throw new RuntimeException(String.format("Module %s not found", moduleName));
                }
                Module target = layer.findModule(e.getValue()).orElse(null);
                if (target == null) {
                    throw new RuntimeException(String.format("Module %s not found", e.getValue()));
                }
                if (options.enableHacks && source.getLayer() != layer) {
                    ModuleHacks.createController(hackLookup, source.getLayer()).addOpens(source, pkg, target);
                } else {
                    controller.addOpens(source, pkg, target);
                }
            }
            for (var e : options.moduleConf.reads.entrySet()) {
                LogHelper.dev("Read module %s to %s", e.getKey(), e.getValue());
                Module source = layer.findModule(e.getKey()).orElse(null);
                if (source == null) {
                    throw new RuntimeException(String.format("Module %s not found", e.getKey()));
                }
                Module target = layer.findModule(e.getValue()).orElse(null);
                if (target == null) {
                    throw new RuntimeException(String.format("Module %s not found", e.getValue()));
                }
                if (options.enableHacks && source.getLayer() != layer) {
                    ModuleHacks.createController(hackLookup, source.getLayer()).addReads(source, target);
                } else {
                    controller.addReads(source, target);
                }
            }
            moduleClassLoader.initializeWithLayer(layer);
        }
    }

    private static void initGson(RuntimeModuleManager modulesManager) {
        AuthRequest.registerProviders();
        GetAvailabilityAuthRequest.registerProviders();
        OptionalAction.registerProviders();
        OptionalTrigger.registerProviders();
        Launcher.gsonManager = new RuntimeGsonManager(modulesManager);
        Launcher.gsonManager.initGson();
    }

    private static void registerCommandsApp() {
        ConsoleManager.handler.registerCommand("getpublickey", new GetPublicKeyCommand());
        ConsoleManager.handler.registerCommand("signdata", new SignDataCommand());
        ConsoleManager.handler.registerCommand("modules", new ModulesCommand());
    }

    private static void readKeys() throws IOException, InvalidKeySpecException {
        if (privateKey != null || publicKey != null) return;
        Path dir = DirBridge.dir;
        Path publicKeyFile = dir.resolve("public.key");
        Path privateKeyFile = dir.resolve("private.key");
        if (IOHelper.isFile(publicKeyFile) && IOHelper.isFile(privateKeyFile)) {
            LogHelper.info("Reading EC keypair");
            publicKey = SecurityHelper.toPublicECDSAKey(IOHelper.read(publicKeyFile));
            privateKey = SecurityHelper.toPrivateECDSAKey(IOHelper.read(privateKeyFile));
        } else {
            LogHelper.info("Generating EC keypair");
            KeyPair pair = SecurityHelper.genECDSAKeyPair(new SecureRandom());
            publicKey = (ECPublicKey) pair.getPublic();
            privateKey = (ECPrivateKey) pair.getPrivate();

            // Write key pair list
            LogHelper.info("Writing EC keypair list");
            IOHelper.write(publicKeyFile, publicKey.getEncoded());
            IOHelper.write(privateKeyFile, privateKey.getEncoded());
        }
    }

    private static RequestService initOffline() {
        OfflineRequestService service = new OfflineRequestService();
        ClientLauncherMethods.applyBasicOfflineProcessors(service);
        OfflineModeEvent event = new OfflineModeEvent(service);
        modulesManager.invokeEvent(event);
        return event.service;
    }

    private static void preGuiPhase() {
        try {
            Class.forName("javafx.application.Application");
        } catch (ClassNotFoundException e) {
            noJavaFxAlert();
            exitLauncher(0);
        }
        try {
            Method m = JavaFXApplication.class.getMethod(new String(Base64.getDecoder().decode("c3RhcnQ=")), Stage.class); // Fix proguard remapping
            if (m.getDeclaringClass() != JavaFXApplication.class)
                throw new RuntimeException("Method startApplication not override");
        } catch (Throwable exception) {
            LogHelper.error(exception);
            noInitMethodAlert();
            exitLauncher(0);
        }
    }

    private static void noJavaFxAlert() {
        String message = """
                Библиотеки JavaFX не найдены. У вас %s(x%d) ОС %s(x%d). Java %s. Установите Java с поддержкой JavaFX
                Если вы не можете решить проблему самостоятельно обратитесь к администрации своего проекта
                """.formatted(JVMHelper.RUNTIME_MXBEAN.getVmName(), JVMHelper.JVM_BITS, JVMHelper.OS_TYPE.name,
                JVMHelper.OS_BITS, JVMHelper.RUNTIME_MXBEAN.getSpecVersion());
        JOptionPane.showMessageDialog(null, message, "GravitLauncher", JOptionPane.ERROR_MESSAGE);
    }

    private static void noInitMethodAlert() {
        String message = """
                JavaFX приложение собрано некорректно. Обратитесь к администратору проекта с скриншотом этого окна
                Описание:
                При сборке отсутствовали библиотеки JavaFX. Пожалуйста установите Java с поддержкой JavaFX на стороне лаунчсервера и повторите сборку лаунчера
                """;
        JOptionPane.showMessageDialog(null, message, "GravitLauncher", JOptionPane.ERROR_MESSAGE);
    }

    private static void verifyNoAgent() {
        // TODO enable this
//        if (JVMHelper.RUNTIME_MXBEAN.getInputArguments().stream().filter(e -> e != null && !e.isEmpty()).anyMatch(e -> e.contains("javaagent")))
//            throw new SecurityException("JavaAgent found");
    }

    private static void checkClass(Class<?> clazz) throws SecurityException {
        // TODO enable this
        LauncherTrustManager trustManager = Launcher.getConfig().trustManager;
        if (trustManager == null) return;
        X509Certificate[] certificates = getCertificates(clazz);
        if (certificates == null) {
            // TODO enable this
//            throw new SecurityException(String.format("Class %s not signed", clazz.getName()));
        }
        try {
            trustManager.checkCertificatesSuccess(certificates, trustManager::stdCertificateChecker);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    private static X509Certificate[] getCertificates(Class<?> clazz) {
        return JVMHelper.getCertificates(clazz);
    }

    public static ECPublicKey getClientPublicKey() {
        return publicKey;
    }

    public static byte[] sign(byte[] bytes) {
        return SecurityHelper.sign(bytes, privateKey);
    }

    public static void exitLauncher(int code) {
        beforeExit(code);
        forceExit(code);
    }

    public static void beforeExit(int code) {
        try {
            modulesManager.invokeEvent(new ClientExitPhase(code));
        } catch (Throwable ignored) {
        }
    }

    public static void forceExit(int code) {
        try {
            System.exit(code);
        } catch (Throwable e) //Forge Security Manager?
        {
            NativeJVMHalt.haltA(code);
        }
    }

    public static boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) {
                return true;
            }
        }
        return false;
    }
}