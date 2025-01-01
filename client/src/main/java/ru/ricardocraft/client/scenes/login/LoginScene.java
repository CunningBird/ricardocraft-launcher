package ru.ricardocraft.client.scenes.login;

import javafx.application.Platform;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.dto.response.AuthRequestEvent;
import ru.ricardocraft.client.dto.response.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.client.profiles.Texture;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.response.WebSocketEvent;
import ru.ricardocraft.client.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.client.dto.request.update.LauncherRequest;
import ru.ricardocraft.client.dto.request.update.ProfilesRequest;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.launch.RuntimeSecurityService;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.overlays.WelcomeOverlay;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.runtime.utils.LauncherUpdater;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.options.OptionsScene;
import ru.ricardocraft.client.scenes.servermenu.ServerMenuScene;
import ru.ricardocraft.client.scenes.settings.GlobalSettingsScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.helper.LogHelper;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public abstract class LoginScene extends AbstractScene {

    public static volatile Path updatePath;

    private final RuntimeSettings runtimeSettings;
    private final LauncherConfig config;
    private final GuiModuleConfig guiModuleConfig;
    private final SkinManager skinManager;
    private final RuntimeSecurityService securityService;

    private List<GetAvailabilityAuthRequestEvent.AuthAvailability> auth; //TODO: FIX? Field is assigned but never accessed.
    private CheckBox savePasswordCheckBox;
    private CheckBox autoenter;
    private Pane content;
    private AbstractVisualComponent contentComponent;
    private LoginAuthButtonComponent authButton;
    private ComboBox<GetAvailabilityAuthRequestEvent.AuthAvailability> authList;
    private GetAvailabilityAuthRequestEvent.AuthAvailability authAvailability;
    private final AuthFlow authFlow;

    public LoginScene(LauncherConfig config,
                      GuiModuleConfig guiModuleConfig,
                      SettingsManager settingsManager,
                      AuthService authService,
                      SkinManager skinManager,
                      LaunchService launchService,
                      RuntimeSecurityService securityService) {
        super("scenes/login/login.fxml", config, guiModuleConfig, authService, launchService, settingsManager);
        this.config = config;
        this.guiModuleConfig = guiModuleConfig;
        this.skinManager = skinManager;
        this.securityService = securityService;
        LoginSceneAccessor accessor = new LoginSceneAccessor();
        this.runtimeSettings = settingsManager.getRuntimeSettings();
        this.authFlow = new AuthFlow(accessor, this::onSuccessLogin, runtimeSettings, guiModuleConfig, authService, launchService) {
            @Override
            protected WebAuthOverlay getWebAuthOverlay() {
                return LoginScene.this.getWebAuthOverlay();
            }
        };
    }

    abstract protected GlobalSettingsScene getGlobalSettingsScene();

    abstract protected WelcomeOverlay geWelcomeOverlay();

    abstract protected OptionsScene getOptionsScene();

    abstract protected ServerMenuScene getServerMenuScene();

    abstract protected WebAuthOverlay getWebAuthOverlay();

    abstract protected AbstractScene getCurrentScene();

    abstract protected void setMainScene(AbstractScene scene) throws Exception;

    abstract protected void openUrl(String url);

    @Override
    public void doInit() {
        LookupHelper.<ButtonBase>lookup(header, "#controls", "#settings").setOnAction((e) -> {
            try {
                switchScene(getGlobalSettingsScene());
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });
        authButton = new LoginAuthButtonComponent(LookupHelper.lookup(layout, "#authButton"), (e) -> contextHelper.runCallback(authFlow::loginWithGui));
        savePasswordCheckBox = LookupHelper.lookup(layout, "#savePassword");
        if (runtimeSettings.password != null || runtimeSettings.oauthAccessToken != null) {
            LookupHelper.<CheckBox>lookup(layout, "#savePassword").setSelected(true);
        }
        autoenter = LookupHelper.lookup(layout, "#autoenter");
        autoenter.setSelected(runtimeSettings.autoAuth);
        autoenter.setOnAction((event) -> runtimeSettings.autoAuth = autoenter.isSelected());
        content = LookupHelper.lookup(layout, "#content");
        if (guiModuleConfig.createAccountURL != null) {
            LookupHelper.<Text>lookup(header, "#createAccount")
                    .setOnMouseClicked((e) -> openUrl(guiModuleConfig.createAccountURL));
        }

        if (guiModuleConfig.forgotPassURL != null) {
            LookupHelper.<Text>lookup(header, "#forgotPass")
                    .setOnMouseClicked((e) -> openUrl(guiModuleConfig.forgotPassURL));
        }
        authList = LookupHelper.lookup(layout, "#authList");
        authList.setConverter(new AuthAvailabilityStringConverter());
        authList.setOnAction((e) -> changeAuthAvailability(authList.getSelectionModel().getSelectedItem()));
        authFlow.prepare();
        // Verify Launcher
    }

    @Override
    protected void doPostInit() {
        launcherRequest();
    }

    @Override
    public void errorHandle(Throwable e) {
        super.errorHandle(e);
        contextHelper.runInFxThread(() -> authButton.setState(LoginAuthButtonComponent.AuthButtonState.ERROR));
    }

    @Override
    public void reset() {
        authFlow.reset();
    }

    @Override
    public String getName() {
        return "login";
    }

    private void launcherRequest() {
        LauncherRequest launcherRequest = new LauncherRequest(config);
        processRequest(launchService.getTranslation("runtime.overlay.processing.text.launcher"), launcherRequest,
                (result) -> {
                    if (result.needUpdate) {
                        try {
                            LogHelper.debug("Start update processing");
                            disable();
                            updatePath = LauncherUpdater.prepareUpdate(new URI(result.url).toURL());
                            LogHelper.debug("Exit with Platform.exit");
                            Platform.exit();
                            return;
                        } catch (Throwable e) {
                            contextHelper.runInFxThread(() -> errorHandle(e));
                            try {
                                Thread.sleep(1500);
                                settingsManager.saveSettings();
                                Platform.exit();
                            } catch (Throwable ex) {
                                settingsManager.exitLauncher(0);
                            }
                        }
                    }
                    LogHelper.dev("Launcher update processed");
                    getAvailabilityAuth();
                }, (event) -> settingsManager.exitLauncher(0));
    }

    private void getAvailabilityAuth() {
        GetAvailabilityAuthRequest getAvailabilityAuthRequest = new GetAvailabilityAuthRequest();
        processing(getAvailabilityAuthRequest,
                launchService.getTranslation("runtime.overlay.processing.text.authAvailability"),
                (auth) -> contextHelper.runInFxThread(() -> {
                    this.auth = auth.list;
                    authList.setVisible(auth.list.size() != 1);
                    authList.setManaged(auth.list.size() != 1);
                    for (GetAvailabilityAuthRequestEvent.AuthAvailability authAvailability : auth.list) {
                        if (!authAvailability.visible) {
                            continue;
                        }
                        if (runtimeSettings.lastAuth == null) {
                            if (authAvailability.name.equals("std") || this.authAvailability == null) {
                                changeAuthAvailability(authAvailability);
                            }
                        } else if (authAvailability.name.equals(runtimeSettings.lastAuth.name))
                            changeAuthAvailability(authAvailability);
                        if (authAvailability.visible) {
                            addAuthAvailability(authAvailability);
                        }
                    }
                    if (this.authAvailability == null && !auth.list.isEmpty()) {
                        changeAuthAvailability(auth.list.get(0));
                    }
                    runAutoAuth();
                }), null);
    }

    private void runAutoAuth() {
        if (guiModuleConfig.autoAuth || runtimeSettings.autoAuth) {
            contextHelper.runInFxThread(authFlow::loginWithGui);
        }
    }

    public void changeAuthAvailability(GetAvailabilityAuthRequestEvent.AuthAvailability authAvailability) {
        boolean isChanged = this.authAvailability != authAvailability; //TODO: FIX
        this.authAvailability = authAvailability;
        this.authService.setAuthAvailability(authAvailability);
        this.authList.selectionModelProperty().get().select(authAvailability);
        authFlow.init(authAvailability);
        LogHelper.info("Selected auth: %s", authAvailability.name);
    }

    public void addAuthAvailability(GetAvailabilityAuthRequestEvent.AuthAvailability authAvailability) {
        authList.getItems().add(authAvailability);
        LogHelper.info("Added %s: %s", authAvailability.name, authAvailability.displayName);
    }

    public <T extends WebSocketEvent> void processing(Request<T> request, String text, Consumer<T> onSuccess,
                                                      Consumer<String> onError) {
        processRequest(text, request, onSuccess, (thr) -> onError.accept(thr.getCause().getMessage()), null);
    }


    public void onSuccessLogin(AuthFlow.SuccessAuth successAuth) {
        AuthRequestEvent result = successAuth.requestEvent();
        authService.setAuthResult(authAvailability.name, result);
        boolean savePassword = savePasswordCheckBox.isSelected();
        if (savePassword) {
            runtimeSettings.login = successAuth.recentLogin();
            if (result.oauth == null) {
                LogHelper.warning("Password not saved");
            } else {
                runtimeSettings.oauthAccessToken = result.oauth.accessToken;
                runtimeSettings.oauthRefreshToken = result.oauth.refreshToken;
                runtimeSettings.oauthExpire = Request.getTokenExpiredTime();
                runtimeSettings.password = null;
            }
            runtimeSettings.lastAuth = authAvailability;
        }
        if (result.playerProfile != null
                && result.playerProfile.assets != null) {
            try {
                Texture skin = result.playerProfile.assets.get("SKIN");
                Texture avatar = result.playerProfile.assets.get("AVATAR");
                if (skin != null || avatar != null) {
                    skinManager.addSkinWithAvatar(result.playerProfile.username,
                            skin != null ? new URI(skin.url) : null,
                            avatar != null ? new URI(avatar.url) : null);
                    skinManager.getSkin(result.playerProfile.username); //Cache skin
                }
            } catch (Exception e) {
                LogHelper.error(e);
            }
        }
        contextHelper.runInFxThread(() -> {
            WelcomeOverlay welcomeOverlay = geWelcomeOverlay();

            if (welcomeOverlay.isInit()) {
                welcomeOverlay.reset();
            }
            showOverlay(welcomeOverlay, (e) -> welcomeOverlay.hide(2000, (f) -> onGetProfiles()));
        });
    }

    public void onGetProfiles() {
        processing(new ProfilesRequest(), launchService.getTranslation("runtime.overlay.processing.text.profiles"),
                (profiles) -> {
                    settingsManager.setProfilesResult(profiles);
                    runtimeSettings.profiles = profiles.profiles;
                    contextHelper.runInFxThread(() -> {
                        securityService.startRequest();
                        if (getOptionsScene() != null) {
                            try {
                                settingsManager.loadAll();
                            } catch (Throwable ex) {
                                errorHandle(ex);
                            }
                        }
                        if (getCurrentScene() instanceof LoginScene loginScene) {
                            loginScene.authFlow.isLoginStarted = false;
                        }
                        setMainScene(getServerMenuScene());
                    });
                }, null);
    }

    public void clearPassword() {
        runtimeSettings.password = null;
        runtimeSettings.login = null;
        runtimeSettings.oauthAccessToken = null;
        runtimeSettings.oauthRefreshToken = null;
    }

    private static class AuthAvailabilityStringConverter extends StringConverter<GetAvailabilityAuthRequestEvent.AuthAvailability> {
        @Override
        public String toString(GetAvailabilityAuthRequestEvent.AuthAvailability object) {
            return object == null ? "null" : object.displayName;
        }

        @Override
        public GetAvailabilityAuthRequestEvent.AuthAvailability fromString(String string) {
            return null;
        }
    }

    public class LoginSceneAccessor extends SceneAccessor {

        public void showContent(AbstractVisualComponent component) throws Exception {
            component.init();
            component.postInit();
            if (contentComponent != null) {
                content.getChildren().clear();
            }
            contentComponent = component;
            content.getChildren().add(component.getLayout());
        }

        public LoginAuthButtonComponent getAuthButton() {
            return authButton;
        }

        public void setState(LoginAuthButtonComponent.AuthButtonState state) {
            authButton.setState(state);
        }

        public boolean isEmptyContent() {
            return content.getChildren().isEmpty();
        }

        public void clearContent() {
            content.getChildren().clear();
        }

        public <T extends WebSocketEvent> void processing(Request<T> request, String text, Consumer<T> onSuccess,
                                                          Consumer<String> onError) {
            LoginScene.this.processing(request, text, onSuccess, onError);
        }
    }


}
