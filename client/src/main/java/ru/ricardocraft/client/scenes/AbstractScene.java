package ru.ricardocraft.client.scenes;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.WebSocketEvent;
import ru.ricardocraft.client.dto.request.auth.ExitRequest;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.overlays.AbstractOverlay;
import ru.ricardocraft.client.overlays.ProcessingOverlay;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;

import java.util.function.Consumer;

public abstract class AbstractScene extends AbstractVisualComponent {
    protected Pane header;

    protected final LauncherConfig launcherConfig;
    protected final AuthService authService;
    protected final SettingsManager settingsManager;

    protected AbstractScene(String fxmlPath,
                            JavaFXApplication application,
                            LauncherConfig config,
                            GuiModuleConfig guiModuleConfig,
                            AuthService authService,
                            LaunchService launchService,
                            SettingsManager settingsManager) {
        super(fxmlPath, application, guiModuleConfig, launchService);
        this.launcherConfig = config;
        this.authService = authService;
        this.settingsManager = settingsManager;
    }

    public void init() throws Exception {
        layout = (Pane) getFxmlRoot();
        header = (Pane) LookupHelper.lookupIfPossible(layout, "#header").orElse(null);
        sceneBaseInit();
        super.init();
    }

    protected abstract void doInit();

    @Override
    protected void doPostInit() {

    }

    protected void onShow() {

    }

    protected void onHide() {

    }


    public void showOverlay(AbstractOverlay overlay, EventHandler<ActionEvent> onFinished) throws Exception {
        overlay.show(currentStage, onFinished);
    }

    protected final <T extends WebSocketEvent> void processRequest(String message, Request<T> request,
                                                                   Consumer<T> onSuccess, EventHandler<ActionEvent> onError) {
        getProcessingOverlay().processRequest(currentStage, message, request, onSuccess, onError);
    }

    protected final <T extends WebSocketEvent> void processRequest(String message, Request<T> request,
                                                                   Consumer<T> onSuccess, Consumer<Throwable> onException, EventHandler<ActionEvent> onError) {
        getProcessingOverlay().processRequest(currentStage, message, request, onSuccess, onException, onError);
    }

    protected ProcessingOverlay getProcessingOverlay() {
        return (ProcessingOverlay) application.gui.getByName("processing");
    }

    protected LoginScene getLoginScene() {
        return (LoginScene) application.gui.getByName("login");
    }

    protected void sceneBaseInit() {
        initBasicControls(header);
        LookupHelper.<ButtonBase>lookupIfPossible(header, "#controls", "#deauth").ifPresent(b -> b.setOnAction(
                (e) -> launchService.showApplyDialog(
                        launchService.getTranslation("runtime.scenes.settings.exitDialog.header"),
                        launchService.getTranslation("runtime.scenes.settings.exitDialog.description"),
                        this::userExit, () -> {
                        }, true)));
    }

    protected void userExit() {
        processRequest(launchService.getTranslation("runtime.scenes.settings.exitDialog.processing"), new ExitRequest(),
                (event) -> {
                    // Exit to main menu
                    ContextHelper.runInFxThreadStatic(() -> {
                        LoginScene loginScene = getLoginScene();
                        loginScene.clearPassword();
                        loginScene.reset();
                        try {
                            settingsManager.saveSettings();
                            authService.exit();
                            switchScene(loginScene);
                        } catch (Exception ex) {
                            errorHandle(ex);
                        }
                    });
                }, (event) -> {
                });
    }

    protected void switchToBackScene() throws Exception {
        currentStage.back();
    }

    public void disable() {
        currentStage.disable();
    }

    public void enable() {
        currentStage.enable();
    }

    public abstract void reset();

    protected void switchScene(AbstractScene scene) throws Exception {
        currentStage.setScene(scene, true);
        onHide();
        scene.onShow();
    }

    public Node getHeader() {
        return header;
    }

    public class SceneAccessor {
        public SceneAccessor() {
        }


        public void showOverlay(AbstractOverlay overlay, EventHandler<ActionEvent> onFinished) throws Exception {
            AbstractScene.this.showOverlay(overlay, onFinished);
        }

        public JavaFXApplication getApplication() {
            return application;
        }

        public void errorHandle(Throwable e) {
            AbstractScene.this.errorHandle(e);
        }

        public void runInFxThread(ContextHelper.GuiExceptionRunnable runnable) {
            contextHelper.runInFxThread(runnable);
        }

        public final <T extends WebSocketEvent> void processRequest(String message, Request<T> request,
                                                                    Consumer<T> onSuccess, Consumer<Throwable> onException, EventHandler<ActionEvent> onError) {
            AbstractScene.this.processRequest(message, request, onSuccess, onException, onError);
        }
    }
}
