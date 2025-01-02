package ru.ricardocraft.client.configuration;

import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.base.helper.LogHelper;
import ru.ricardocraft.client.ui.impl.AbstractVisualComponent;
import ru.ricardocraft.client.ui.impl.BackgroundComponent;
import ru.ricardocraft.client.ui.impl.ContextHelper;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.ui.scenes.AbstractScene;
import ru.ricardocraft.client.ui.scenes.login.LoginScene;
import ru.ricardocraft.client.ui.stage.PrimaryStage;

import static ru.ricardocraft.client.configuration.VisualComponentScopeConfigurer.components;
import static ru.ricardocraft.client.base.helper.EnFSHelper.resetDirectory;

public abstract class GuiObjectsContainer {

    private final LauncherConfig config;
    private final SettingsManager settingsManager;

    private JavaFXApplication application;
    private PrimaryStage mainStage;

    @Autowired
    public GuiObjectsContainer(LauncherConfig config, SettingsManager settingsManager) {
        this.config = config;
        this.settingsManager = settingsManager;
    }

    abstract protected BackgroundComponent getBackgroundComponent();

    abstract protected LoginScene getLoginScene();

    abstract protected AbstractVisualComponent getByName(String name);

    public void setupPrimaryStage(JavaFXApplication application, Stage stage) throws Exception {
        this.application = application;
        this.mainStage = new PrimaryStage(stage, "%s Launcher".formatted(config.projectName)) {
            @Override
            protected AbstractVisualComponent getByName(String name) {
                return GuiObjectsContainer.this.getByName(name);
            }
        };

        mainStage.setScene(getLoginScene(), true);
        BackgroundComponent backgroundComponent = getBackgroundComponent();
        backgroundComponent.init();
        mainStage.pushBackground(backgroundComponent);
        mainStage.show();
    }

    public void openURL(String url) {
        try {
            application.getHostServices().showDocument(url);
        } catch (Throwable e) {
            LogHelper.error(e);
        }
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

    public void reload() throws Exception {
        String sceneName = mainStage.getVisualComponent().getName();
        ContextHelper.runInFxThreadStatic(() -> {
            mainStage.setScene(null, false);
            BackgroundComponent backgroundComponent = getBackgroundComponent();
            mainStage.pullBackground(backgroundComponent);
            resetDirectory(config, settingsManager.getRuntimeSettings());
            components.clear();
            mainStage.resetStyles();
            mainStage.pushBackground(backgroundComponent);
            mainStage.setScene(getByName(sceneName), false);
        }).get();
    }
}
