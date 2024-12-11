package ru.ricardocraft.client.impl;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.overlays.ProcessingOverlay;
import ru.ricardocraft.client.overlays.UploadAssetOverlay;
import ru.ricardocraft.client.overlays.WelcomeOverlay;
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
import ru.ricardocraft.client.stage.ConsoleStage;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GuiObjectsContainer {
    private final JavaFXApplication application;
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

    public GuiObjectsContainer(JavaFXApplication application) {
        this.application = application;
    }

    public void init() {
        background = registerComponent(BackgroundComponent.class);
        loginScene = registerComponent(LoginScene.class);
        processingOverlay = registerComponent(ProcessingOverlay.class);
        welcomeOverlay = registerComponent(WelcomeOverlay.class);
        uploadAssetOverlay = registerComponent(UploadAssetOverlay.class);

        serverMenuScene = registerComponent(ServerMenuScene.class);
        serverInfoScene = registerComponent(ServerInfoScene.class);
        optionsScene = registerComponent(OptionsScene.class);
        settingsScene = registerComponent(SettingsScene.class);
        globalSettingsScene = registerComponent(GlobalSettingsScene.class);
        consoleScene = registerComponent(ConsoleScene.class);

        updateScene = registerComponent(UpdateScene.class);
        debugScene = registerComponent(DebugScene.class);
        browserScene = registerComponent(BrowserScene.class);
    }

    public Collection<AbstractVisualComponent> getComponents() {
        return components.values();
    }

    public void reload() throws Exception {
        String sceneName = application.getCurrentScene().getName();
        ContextHelper.runInFxThreadStatic(() -> {
            application.getMainStage().setScene(null, false);
            application.getMainStage().pullBackground(background);
            application.resetDirectory();
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

    @SuppressWarnings("unchecked")
    public <T extends AbstractVisualComponent> T registerComponent(Class<T> clazz) {
        try {
            T instance = (T) MethodHandles
                    .publicLookup().findConstructor(clazz, MethodType.methodType(void.class, JavaFXApplication.class))
                    .invokeWithArguments(application);
            components.put(instance.getName(), instance);
            return instance;
        } catch (Throwable e) {
            LogHelper.error(e);
            throw new RuntimeException(e);
        }
    }
}
