package ru.ricardocraft.client.impl;

import javafx.stage.Stage;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.overlays.ProcessingOverlay;
import ru.ricardocraft.client.overlays.UploadAssetOverlay;
import ru.ricardocraft.client.overlays.WelcomeOverlay;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.console.ConsoleScene;
import ru.ricardocraft.client.scenes.debug.DebugScene;
import ru.ricardocraft.client.scenes.internal.BrowserScene;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.scenes.login.WebAuthOverlay;
import ru.ricardocraft.client.scenes.options.OptionsScene;
import ru.ricardocraft.client.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.scenes.servermenu.ServerMenuScene;
import ru.ricardocraft.client.scenes.settings.GlobalSettingsScene;
import ru.ricardocraft.client.scenes.settings.SettingsScene;
import ru.ricardocraft.client.scenes.update.UpdateScene;
import ru.ricardocraft.client.stage.PrimaryStage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.ricardocraft.client.helper.EnFSHelper.resetDirectory;

@Component
public class GuiObjectsContainer {

    private final JavaFXApplication application = JavaFXApplication.getInstance();

    private final LauncherConfig config;
    private final SettingsManager settingsManager;

    private PrimaryStage mainStage;

    private final ObjectFactory<WebAuthOverlay> webAuthOverlayObjectFactory;
    private final ObjectFactory<BackgroundComponent> backgroundComponentObjectFactory;
    private final ObjectFactory<LoginScene> loginSceneObjectFactory;
    private final ObjectFactory<ProcessingOverlay> processingOverlayObjectFactory;
    private final ObjectFactory<WelcomeOverlay> welcomeOverlayObjectFactory;
    private final ObjectFactory<UploadAssetOverlay> uploadAssetOverlayObjectFactory;
    private final ObjectFactory<ServerMenuScene> serverMenuSceneObjectFactory;
    private final ObjectFactory<ServerInfoScene> serverInfoSceneObjectFactory;
    private final ObjectFactory<OptionsScene> optionsSceneObjectFactory;
    private final ObjectFactory<SettingsScene> settingsSceneObjectFactory;
    private final ObjectFactory<GlobalSettingsScene> globalSettingsSceneObjectFactory;
    private final ObjectFactory<ConsoleScene> consoleSceneObjectFactory;
    private final ObjectFactory<UpdateScene> updateSceneObjectFactory;
    private final ObjectFactory<DebugScene> debugSceneObjectFactory;
    private final ObjectFactory<BrowserScene> browserSceneObjectFactory;

    private final Map<String, AbstractVisualComponent> components = new HashMap<>();

    @Autowired
    public GuiObjectsContainer(LauncherConfig config,
                               SettingsManager settingsManager,

                               ObjectFactory<WebAuthOverlay> webAuthOverlayObjectFactory,
                               ObjectFactory<BackgroundComponent> backgroundComponentObjectFactory,
                               ObjectFactory<LoginScene> loginSceneObjectFactory,
                               ObjectFactory<ProcessingOverlay> processingOverlayObjectFactory,
                               ObjectFactory<WelcomeOverlay> welcomeOverlayObjectFactory,
                               ObjectFactory<UploadAssetOverlay> uploadAssetOverlayObjectFactory,
                               ObjectFactory<ServerMenuScene> serverMenuSceneObjectFactory,
                               ObjectFactory<ServerInfoScene> serverInfoSceneObjectFactory,
                               ObjectFactory<OptionsScene> optionsSceneObjectFactory,
                               ObjectFactory<SettingsScene> settingsSceneObjectFactory,
                               ObjectFactory<GlobalSettingsScene> globalSettingsSceneObjectFactory,
                               ObjectFactory<ConsoleScene> consoleSceneObjectFactory,
                               ObjectFactory<UpdateScene> updateSceneObjectFactory,
                               ObjectFactory<DebugScene> debugSceneObjectFactory,
                               ObjectFactory<BrowserScene> browserSceneObjectFactory) {
        this.config = config;
        this.settingsManager = settingsManager;

        this.webAuthOverlayObjectFactory = webAuthOverlayObjectFactory;
        this.backgroundComponentObjectFactory = backgroundComponentObjectFactory;
        this.loginSceneObjectFactory = loginSceneObjectFactory;
        this.processingOverlayObjectFactory = processingOverlayObjectFactory;
        this.welcomeOverlayObjectFactory = welcomeOverlayObjectFactory;
        this.uploadAssetOverlayObjectFactory = uploadAssetOverlayObjectFactory;
        this.serverMenuSceneObjectFactory = serverMenuSceneObjectFactory;
        this.serverInfoSceneObjectFactory = serverInfoSceneObjectFactory;
        this.optionsSceneObjectFactory = optionsSceneObjectFactory;
        this.settingsSceneObjectFactory = settingsSceneObjectFactory;
        this.globalSettingsSceneObjectFactory = globalSettingsSceneObjectFactory;
        this.consoleSceneObjectFactory = consoleSceneObjectFactory;
        this.updateSceneObjectFactory = updateSceneObjectFactory;
        this.debugSceneObjectFactory = debugSceneObjectFactory;
        this.browserSceneObjectFactory = browserSceneObjectFactory;
    }

    public void init() {
        registerComponent(webAuthOverlayObjectFactory.getObject());
        registerComponent(backgroundComponentObjectFactory.getObject());
        registerComponent(loginSceneObjectFactory.getObject());
        registerComponent(processingOverlayObjectFactory.getObject());
        registerComponent(welcomeOverlayObjectFactory.getObject());
        registerComponent(uploadAssetOverlayObjectFactory.getObject());
        registerComponent(serverMenuSceneObjectFactory.getObject());
        registerComponent(serverInfoSceneObjectFactory.getObject());
        registerComponent(optionsSceneObjectFactory.getObject());
        registerComponent(settingsSceneObjectFactory.getObject());
        registerComponent(globalSettingsSceneObjectFactory.getObject());
        registerComponent(consoleSceneObjectFactory.getObject());
        registerComponent(updateSceneObjectFactory.getObject());
        registerComponent(debugSceneObjectFactory.getObject());
        registerComponent(browserSceneObjectFactory.getObject());
    }

    public void setupPrimaryStage(Stage stage) throws Exception {
        mainStage = new PrimaryStage(this, stage, "%s Launcher".formatted(config.projectName));
        init();
        mainStage.setScene(getByName("login"), true);
        BackgroundComponent backgroundComponent = (BackgroundComponent) getByName("background");
        backgroundComponent.init();
        mainStage.pushBackground(backgroundComponent);
        mainStage.show();
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

    public Collection<AbstractVisualComponent> getComponents() {
        return components.values();
    }

    public void reload() throws Exception {
        String sceneName = application.gui.getCurrentScene().getName();
        ContextHelper.runInFxThreadStatic(() -> {
            getMainStage().setScene(null, false);
            BackgroundComponent backgroundComponent = (BackgroundComponent) getByName("background");
            getMainStage().pullBackground(backgroundComponent);
            resetDirectory(config, settingsManager.getRuntimeSettings());
            components.clear();
            getMainStage().resetStyles();
            init();
            getMainStage().pushBackground(backgroundComponent);
            for (AbstractVisualComponent s : components.values()) {
                if (sceneName.equals(s.getName())) {
                    getMainStage().setScene(s, false);
                }
            }
        }).get();
    }

    public AbstractVisualComponent getByName(String name) {
        return components.get(name);
    }

    public void registerComponent(AbstractVisualComponent component) {
        components.put(component.getName(), component);
    }
}
