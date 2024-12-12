package ru.ricardocraft.client.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.launch.RuntimeSecurityService;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.overlays.ProcessingOverlay;
import ru.ricardocraft.client.overlays.UploadAssetOverlay;
import ru.ricardocraft.client.overlays.WelcomeOverlay;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.console.ConsoleScene;
import ru.ricardocraft.client.scenes.debug.DebugScene;
import ru.ricardocraft.client.scenes.internal.BrowserScene;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.scenes.options.OptionsScene;
import ru.ricardocraft.client.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.scenes.servermenu.ServerMenuScene;
import ru.ricardocraft.client.scenes.settings.GlobalSettingsScene;
import ru.ricardocraft.client.scenes.settings.SettingsScene;
import ru.ricardocraft.client.scenes.update.UpdateScene;
import ru.ricardocraft.client.service.*;
import ru.ricardocraft.client.stage.ConsoleStage;
import ru.ricardocraft.client.utils.command.CommandHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.ricardocraft.client.helper.EnFSHelper.resetDirectory;

@Component
public class GuiObjectsContainer {

    private final JavaFXApplication application = JavaFXApplication.getInstance();

    private final LauncherConfig config;
    private final SettingsManager settingsManager;
    private final GuiModuleConfig guiModuleConfig;
    private final LaunchService launchService;
    private final AuthService authService;
    private final SkinManager skinManager;
    private final RuntimeSecurityService securityService;
    private final ProfilesService profilesService;
    private final RequestService service;
    private final PingService pingService;
    private final JavaService javaService;
    private final TriggerManager triggerManager;
    private final CommandHandler commandHandler;

    private final Map<String, AbstractVisualComponent> components = new HashMap<>();
    public ProcessingOverlay processingOverlay;
    public WelcomeOverlay welcomeOverlay;
    public UploadAssetOverlay uploadAssetOverlay;
    public UpdateScene updateScene;
    public DebugScene debugScene;

    public ServerMenuScene serverMenuScene;
    public ServerInfoScene serverInfoScene;
    public LoginScene loginScene;
    public OptionsScene optionsScene;
    public SettingsScene settingsScene;
    public GlobalSettingsScene globalSettingsScene;
    public ConsoleScene consoleScene;

    public ConsoleStage consoleStage;
    public BrowserScene browserScene;
    public BackgroundComponent background;

    @Autowired
    public GuiObjectsContainer(LauncherConfig config,
                               SettingsManager settingsManager,
                               GuiModuleConfig guiModuleConfig,
                               LaunchService launchService,
                               AuthService authService,
                               SkinManager skinManager,
                               RuntimeSecurityService securityService,
                               ProfilesService profilesService,
                               RequestService service,
                               PingService pingService,
                               JavaService javaService,
                               TriggerManager triggerManager,
                               CommandHandler commandHandler) {
        this.config = config;
        this.settingsManager = settingsManager;
        this.guiModuleConfig = guiModuleConfig;
        this.launchService = launchService;
        this.authService = authService;
        this.skinManager = skinManager;
        this.securityService = securityService;
        this.profilesService = profilesService;
        this.service = service;
        this.pingService = pingService;
        this.javaService = javaService;
        this.triggerManager = triggerManager;
        this.commandHandler = commandHandler;
    }

    public void init() {
        background = new BackgroundComponent(application, guiModuleConfig, launchService);
        loginScene = new LoginScene(application, config, guiModuleConfig, settingsManager, authService, skinManager, launchService, securityService, profilesService);
        processingOverlay = new ProcessingOverlay(application, guiModuleConfig, service, launchService);
        welcomeOverlay = new WelcomeOverlay(application, guiModuleConfig, authService, skinManager, launchService);
        uploadAssetOverlay = new UploadAssetOverlay(application, guiModuleConfig, authService, skinManager, launchService);

        serverMenuScene = new ServerMenuScene(application, config, guiModuleConfig, settingsManager, authService, skinManager, launchService, profilesService, pingService);
        serverInfoScene = new ServerInfoScene(application, config, guiModuleConfig, settingsManager, authService, skinManager, launchService, profilesService, pingService);
        optionsScene = new OptionsScene(application, config, guiModuleConfig, authService, skinManager, launchService, profilesService, pingService);
        settingsScene = new SettingsScene(application, config, guiModuleConfig, settingsManager, authService, skinManager, launchService, profilesService, triggerManager, javaService, pingService);
        globalSettingsScene = new GlobalSettingsScene(application, config, guiModuleConfig, settingsManager, authService, launchService, profilesService, javaService);
        consoleScene = new ConsoleScene(application, config, guiModuleConfig, authService, launchService, commandHandler);

        updateScene = new UpdateScene(application, config, guiModuleConfig, service, authService, launchService);
        debugScene = new DebugScene(application, config, guiModuleConfig, authService, launchService);
        browserScene = new BrowserScene(application, config, guiModuleConfig, authService, launchService);

        components.put(background.getName(), background);
        components.put(loginScene.getName(), loginScene);
        components.put(processingOverlay.getName(), processingOverlay);
        components.put(welcomeOverlay.getName(), welcomeOverlay);
        components.put(uploadAssetOverlay.getName(), uploadAssetOverlay);

        components.put(serverMenuScene.getName(), serverMenuScene);
        components.put(serverInfoScene.getName(), serverInfoScene);
        components.put(optionsScene.getName(), optionsScene);
        components.put(settingsScene.getName(), settingsScene);
        components.put(globalSettingsScene.getName(), globalSettingsScene);
        components.put(consoleScene.getName(), consoleScene);

        components.put(updateScene.getName(), updateScene);
        components.put(debugScene.getName(), debugScene);
        components.put(browserScene.getName(), browserScene);
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

    public void addComponent(String className, AbstractVisualComponent component) {
        components.put(className, component);
    }
}
