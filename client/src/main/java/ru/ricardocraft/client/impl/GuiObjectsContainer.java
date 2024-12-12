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
    private final ObjectFactory<WebAuthOverlay> webAuthOverlayObjectFactory;

    private final Map<String, AbstractVisualComponent> components = new HashMap<>();

    public BackgroundComponent background;

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
        WebAuthOverlay webAuthOverlay = webAuthOverlayObjectFactory.getObject();
        components.put(webAuthOverlay.getName(), webAuthOverlay);

        background = backgroundComponentObjectFactory.getObject();
        components.put(background.getName(), background);

        LoginScene loginScene = loginSceneObjectFactory.getObject();
        components.put(loginScene.getName(), loginScene);

        ProcessingOverlay processingOverlay = processingOverlayObjectFactory.getObject();
        components.put(processingOverlay.getName(), processingOverlay);

        WelcomeOverlay welcomeOverlay = welcomeOverlayObjectFactory.getObject();
        components.put(welcomeOverlay.getName(), welcomeOverlay);

        UploadAssetOverlay uploadAssetOverlay = uploadAssetOverlayObjectFactory.getObject();
        components.put(uploadAssetOverlay.getName(), uploadAssetOverlay);

        ServerMenuScene serverMenuScene = serverMenuSceneObjectFactory.getObject();
        components.put(serverMenuScene.getName(), serverMenuScene);

        ServerInfoScene serverInfoScene = serverInfoSceneObjectFactory.getObject();
        components.put(serverInfoScene.getName(), serverInfoScene);

        OptionsScene optionsScene = optionsSceneObjectFactory.getObject();
        components.put(optionsScene.getName(), optionsScene);

        SettingsScene settingsScene = settingsSceneObjectFactory.getObject();
        components.put(settingsScene.getName(), settingsScene);

        GlobalSettingsScene globalSettingsScene = globalSettingsSceneObjectFactory.getObject();
        components.put(globalSettingsScene.getName(), globalSettingsScene);

        ConsoleScene consoleScene = consoleSceneObjectFactory.getObject();
        components.put(consoleScene.getName(), consoleScene);

        UpdateScene updateScene = updateSceneObjectFactory.getObject();
        components.put(updateScene.getName(), updateScene);

        DebugScene debugScene = debugSceneObjectFactory.getObject();
        components.put(debugScene.getName(), debugScene);

        BrowserScene browserScene = browserSceneObjectFactory.getObject();
        components.put(browserScene.getName(), browserScene);
    }

    public PrimaryStage createPrimaryStage(Stage stage) {
        return new PrimaryStage(this, stage, "%s Launcher".formatted(config.projectName));
    }

    public Collection<AbstractVisualComponent> getComponents() {
        return components.values();
    }

    public void reload() throws Exception {
        String sceneName = application.getCurrentScene().getName();
        ContextHelper.runInFxThreadStatic(() -> {
            application.getMainStage().setScene(null, false);
            application.getMainStage().pullBackground(background);
            resetDirectory(config, settingsManager.getRuntimeSettings());
            components.clear();
            application.getMainStage().resetStyles();
            init();
            application.getMainStage().pushBackground(background);
            for (AbstractVisualComponent s : components.values()) {
                if (sceneName.equals(s.getName())) {
                    application.getMainStage().setScene(s, false);
                }
            }
        }).get();
    }

    public AbstractVisualComponent getByName(String name) {
        return components.get(name);
    }

}
