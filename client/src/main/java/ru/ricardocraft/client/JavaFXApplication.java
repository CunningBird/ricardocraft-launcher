package ru.ricardocraft.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
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
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class JavaFXApplication extends Application {

    private static Path enfsDirectory;
    private static MethodHandles.Lookup hackLookup;
    private static final AtomicReference<JavaFXApplication> INSTANCE = new AtomicReference<>();
    private static final Path runtimeDirectory = IOHelper.WORKING_DIR.resolve("runtime");
    public static RuntimeModuleManager modulesManager = new RuntimeModuleManager();
    public static ECPublicKey publicKey;
    public static ECPrivateKey privateKey;
    public static ModuleLayer layer;
    public static boolean disablePackageDelegateSupport;
    public static final String WAIT_PROCESS_PROPERTY = "launcher.waitProcess";
    public static boolean waitProcess = Boolean.getBoolean(WAIT_PROCESS_PROPERTY);
    public static volatile Path updatePath;

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
    private CommandCategory runtimeCategory;
    private SettingsManager settingsManager;
    private PrimaryStage mainStage;
    private ResourceBundle resources;

    public JavaFXApplication() {
        INSTANCE.set(this);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ECPublicKey getClientPublicKey() {
        return publicKey;
    }

    public static byte[] sign(byte[] bytes) {
        return SecurityHelper.sign(bytes, privateKey);
    }

    @Override
    public void init() throws Exception {
        LogHelper.printVersion("Launcher");
        LogHelper.printLicense("Launcher");
    }

    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext("ru.ricardocraft.client");

//        JVMHelper.checkStackTrace(JavaFXApplication.class); // TODO enable this

        JVMHelper.verifySystemProperties(Launcher.class, true);
        checkClass(JavaFXApplication.class.getClassLoader().getClass());
        EnvHelper.checkDangerousParams();
        // TODO enable this
//        if (JVMHelper.RUNTIME_MXBEAN.getInputArguments().stream().filter(e -> e != null && !e.isEmpty()).anyMatch(e -> e.contains("javaagent")))
//            throw new SecurityException("JavaAgent found");

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


        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        List<Path> classpathList = Stream.of(classpathEntries).map(Path::of).toList();

        // Options
        LaunchOptions options = context.getBean(LaunchOptions.class);
        options.disablePackageDelegateSupport = true;
        options.moduleConf = new LaunchOptions.ModuleConf();

        // Modules
        List<String> jvmModules = List.of(
                "javafx.base",
                "javafx.graphics",
                "javafx.fxml",
                "javafx.controls",
                "javafx.media",
                "javafx.web"
        );
        for (var moduleName : jvmModules) {
            var path = Path.of(System.getProperty("java.home")).resolve("lib").resolve(moduleName.concat(".jar"));
            if (Files.exists(path)) {
                options.moduleConf.modules.add(moduleName);
                options.moduleConf.modulePath.add(path.toAbsolutePath().toString());
            }
        }

        init(classpathList, options);

        LauncherConfig.initModules(modulesManager);
        modulesManager.loadModule(new RuntimeLauncherCoreModule());
        modulesManager.initModules(null);
        // Start Launcher
        AuthRequest.registerProviders();
        GetAvailabilityAuthRequest.registerProviders();
        OptionalAction.registerProviders();
        OptionalTrigger.registerProviders();
        Launcher.gsonManager = new RuntimeGsonManager(modulesManager);
        Launcher.gsonManager.initGson();
        ConsoleManager.initConsole();
        modulesManager.invokeEvent(new PreConfigPhase());

        if (!Request.isAvailable()) {
            String address = config.address;
            LogHelper.debug("Start async connection to %s", address);
            RequestService service;
            try {
                service = StdWebSocketService.initWebSockets(address).get();
            } catch (Throwable e) {
                if (LogHelper.isDebugEnabled()) {
                    LogHelper.error(e);
                }
                LogHelper.warning("Launcher in offline mode");
                OfflineRequestService offlineService = new OfflineRequestService();
                ClientLauncherMethods.applyBasicOfflineProcessors(offlineService);
                OfflineModeEvent event = new OfflineModeEvent(offlineService);
                modulesManager.invokeEvent(event);
                service = event.service;
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

        readKeys();

        ConsoleManager.handler.registerCommand("getpublickey", new GetPublicKeyCommand());
        ConsoleManager.handler.registerCommand("signdata", new SignDataCommand());
        ConsoleManager.handler.registerCommand("modules", new ModulesCommand());

        LogHelper.debug("Dir: %s", DirBridge.dir);
        LogHelper.debug("Start JavaFX Application");


        guiModuleConfig = context.getBean(GuiModuleConfig.class);
        settingsManager = context.getBean(StdSettingsManager.class);

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

        authService = context.getBean(AuthService.class);
        launchService = new LaunchService(this);
        messageManager = new MessageManager(this);

        securityService = new RuntimeSecurityService(service, messageManager);
        skinManager = context.getBean(SkinManager.class);
        javaService = new JavaService(guiModuleConfig);
        triggerManager = new TriggerManager(this, authService, javaService);
        offlineService = new OfflineService(runtimeSettings, guiModuleConfig);
        profilesService = new ProfilesService(triggerManager);
        pingService = context.getBean(PingService.class);

        runtimeCategory = context.getBean(BaseCommandCategory.class);
        runtimeCategory.registerCommand("version", new VersionCommand());
        if (ConsoleManager.isConsoleUnlock) {
            registerPrivateCommands();
        }
        ConsoleManager.handler.registerCategory(new CommandHandler.Category(runtimeCategory, "runtime"));

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
            if (!offlineService.isAvailableOfflineMode()) {
                messageManager.showDialog(getTranslation("runtime.offline.dialog.header"),
                        getTranslation("runtime.offline.dialog.text"),
                        Platform::exit, Platform::exit, false);
                return;
            }
        }

        gui = new GuiObjectsContainer(this);

        mainStage = new PrimaryStage(gui, stage, "%s Launcher".formatted(config.projectName));
        gui.init();
        mainStage.setScene(gui.loginScene, true);
        gui.background.init();
        mainStage.pushBackground(gui.background);
        mainStage.show();

        if (offlineService.isOfflineMode()) {
            messageManager.createNotification(getTranslation("runtime.offline.notification.header"),
                    getTranslation("runtime.offline.notification.text"));
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

        exitLauncher(0);
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

    public static JavaFXApplication getInstance() {
        return INSTANCE.get();
    }

    public static URL getResourceURL(String name) throws IOException {
        if (enfsDirectory != null) {
            return EnFSHelper.getURL(enfsDirectory.resolve(name).toString().replaceAll("\\\\", "/"));
        } else if (runtimeDirectory != null) {
            Path target = runtimeDirectory.resolve(name);
            if (!Files.exists(target)) throw new FileNotFoundException("File runtime/%s not found".formatted(name));
            return target.toUri().toURL();
        } else {
            return Launcher.getResourceURL(name);
        }
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

    private static void checkClass(Class<?> clazz) throws SecurityException {
        // TODO enable this
        LauncherTrustManager trustManager = Launcher.getConfig().trustManager;
        if (trustManager == null) return;
        X509Certificate[] certificates = JVMHelper.getCertificates(clazz);
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
}