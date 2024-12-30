package ru.ricardocraft.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.core.LauncherTrustManager;
import ru.ricardocraft.client.impl.BackgroundComponent;
import ru.ricardocraft.client.impl.GuiObjectsContainer;
import ru.ricardocraft.client.runtime.client.DirBridge;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.service.JavaService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.OfflineService;
import ru.ricardocraft.client.stage.PrimaryStage;
import ru.ricardocraft.client.helper.EnvHelper;
import ru.ricardocraft.client.helper.JVMHelper;
import ru.ricardocraft.client.helper.LogHelper;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import static ru.ricardocraft.client.runtime.utils.LauncherUpdater.launcherBeforeExit;

public class JavaFXApplication extends Application {

    private static MethodHandles.Lookup hackLookup;
    private static final AtomicReference<JavaFXApplication> INSTANCE = new AtomicReference<>();

    public SettingsManager settingsManager;
    public JavaService javaService;
    public OfflineService offlineService;
    public LaunchService launchService;

    public GuiObjectsContainer gui;
    private PrimaryStage mainStage;

    public JavaFXApplication() {
        INSTANCE.set(this);
    }

    public static JavaFXApplication getInstance() {
        return INSTANCE.get();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public AbstractScene getCurrentScene() {
        return (AbstractScene) mainStage.getVisualComponent();
    }

    public PrimaryStage getMainStage() {
        return mainStage;
    }

    public void setMainScene(AbstractScene scene) throws Exception {
        mainStage.setScene(scene, true);
    }

    public void openURL(String url) {
        try {
            getHostServices().showDocument(url);
        } catch (Throwable e) {
            LogHelper.error(e);
        }
    }

    @Override
    public void init() throws Exception {
        LogHelper.printVersion("Launcher");
        LogHelper.printLicense("Launcher");
        LogHelper.debug("Dir: %s", DirBridge.dir);
        LogHelper.debug("Start JavaFX Application");
    }

    @Override
    public void start(Stage stage) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext("ru.ricardocraft.client");
        LauncherConfig config = context.getBean(LauncherConfig.class);

//        JVMHelper.checkStackTrace(JavaFXApplication.class); // TODO enable this

        JVMHelper.verifySystemProperties(Launcher.class, true);
        LauncherTrustManager trustManager = config.trustManager;
        if (trustManager == null) return;
        X509Certificate[] certificates = JVMHelper.getCertificates(JavaFXApplication.class.getClassLoader().getClass());
        if (certificates == null) {
            // TODO enable this
//            throw new SecurityException(String.format("Class %s not signed", clazz.getName()));
        }
        try {
            trustManager.checkCertificatesSuccess(certificates, trustManager::stdCertificateChecker);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
        EnvHelper.checkDangerousParams();
        // TODO enable this
//        if (JVMHelper.RUNTIME_MXBEAN.getInputArguments().stream().filter(e -> e != null && !e.isEmpty()).anyMatch(e -> e.contains("javaagent")))
//            throw new SecurityException("JavaAgent found");


        // TODO enable this
//        Exception e = new Exception();
//        StackTraceElement[] elements = e.getStackTrace();
//        String className = elements[elements.length - 1].getClassName();
//        if (!className.startsWith("ru.ricardocraft.")) {
//            throw new SecurityException(String.format("Untrusted class %s", className));
//        }
//
//        Field trusted = MethodHandles.Lookup.class.getDeclaredField("TRUSTED");
//        trusted.setAccessible(true);
//        int value = (int) trusted.get(null);
//        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
//        constructor.setAccessible(true);
//        hackLookup = constructor.newInstance(JavaFXApplication.class, null, value);

        settingsManager = context.getBean(SettingsManager.class);
        launchService = context.getBean(LaunchService.class);
        javaService = context.getBean(JavaService.class);
        offlineService = context.getBean(OfflineService.class);

        if (offlineService.isOfflineMode() && !offlineService.isAvailableOfflineMode()) {
            launchService.showDialog(
                    launchService.getTranslation("runtime.offline.dialog.header"),
                    launchService.getTranslation("runtime.offline.dialog.text"),
                    Platform::exit,
                    Platform::exit,
                    false
            );
            return;
        }

        gui = context.getBean(GuiObjectsContainer.class);
        mainStage = gui.createPrimaryStage(stage);

        gui.init();
        mainStage.setScene(gui.getByName("login"), true);

        BackgroundComponent backgroundComponent = (BackgroundComponent) gui.getByName("background");
        backgroundComponent.init();
        mainStage.pushBackground(backgroundComponent);
        mainStage.show();

        if (offlineService.isOfflineMode()) {
            launchService.createNotification(
                    launchService.getTranslation("runtime.offline.notification.header"),
                    launchService.getTranslation("runtime.offline.notification.text")
            );
        }
    }

    @Override
    public void stop() throws IOException {
        LogHelper.debug("JavaFX method stop invoked");
        settingsManager.saveSettings();

        LogHelper.debug("Post Application.getLaunchProcess method invoked");
        launcherBeforeExit();

        settingsManager.exitLauncher(0);
    }
}