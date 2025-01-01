package ru.ricardocraft.client.scenes.login.methods;

import javafx.scene.control.TextField;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.scenes.login.AuthFlow;
import ru.ricardocraft.client.scenes.login.LoginAuthButtonComponent;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.helper.LogHelper;

import java.util.concurrent.CompletableFuture;

public class LoginAndPasswordAuthMethod extends AbstractAuthMethod<AuthPasswordDetails> {
    private final LoginAndPasswordOverlay overlay;
    private final RuntimeSettings runtimeSettings;
    private final LaunchService launchService;
    private final LoginScene.LoginSceneAccessor accessor;

    public LoginAndPasswordAuthMethod(LoginScene.LoginSceneAccessor accessor,
                                      RuntimeSettings runtimeSettings,
                                      GuiModuleConfig guiModuleConfig,
                                      AuthService authService,
                                      LaunchService launchService) {
        this.accessor = accessor;
        this.runtimeSettings = runtimeSettings;
        this.launchService = launchService;
        this.overlay = new LoginAndPasswordOverlay(guiModuleConfig, authService, launchService);
    }

    @Override
    public void prepare() {
    }

    @Override
    public void reset() {
        overlay.reset();
    }

    @Override
    public CompletableFuture<Void> show(AuthPasswordDetails details) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            //accessor.showOverlay(overlay, (e) -> future.complete(null));
            ContextHelper.runInFxThreadStatic(() -> {
                accessor.showContent(overlay);
                future.complete(null);
            }).exceptionally((th) -> {
                LogHelper.error(th);
                return null;
            });
        } catch (Exception e) {
            accessor.errorHandle(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<AuthFlow.LoginAndPasswordResult> auth(AuthPasswordDetails details) {
        overlay.future = new CompletableFuture<>();
        String login = overlay.login.getText();
        AuthRequest.AuthPasswordInterface password;
        if (overlay.password.getText().isEmpty() && overlay.password.getPromptText().equals(launchService.getTranslation(
                "runtime.scenes.login.password.saved"))) {
            password = runtimeSettings.password;
            return CompletableFuture.completedFuture(new AuthFlow.LoginAndPasswordResult(login, password));
        }
        return overlay.future;
    }

    @Override
    public void onAuthClicked() {
        overlay.future.complete(overlay.getResult());
    }

    @Override
    public void onUserCancel() {
        overlay.future.completeExceptionally(LoginAndPasswordOverlay.USER_AUTH_CANCELED_EXCEPTION);
    }

    @Override
    public CompletableFuture<Void> hide() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isOverlay() {
        return false;
    }

    public class LoginAndPasswordOverlay extends AbstractVisualComponent {
        private static final UserAuthCanceledException USER_AUTH_CANCELED_EXCEPTION = new UserAuthCanceledException();
        private TextField login;
        private TextField password;
        private CompletableFuture<AuthFlow.LoginAndPasswordResult> future;

        private final AuthService authService;

        public LoginAndPasswordOverlay(GuiModuleConfig guiModuleConfig, AuthService authService, LaunchService launchService) {
            super("scenes/login/methods/loginpassword.fxml", guiModuleConfig, launchService);
            this.authService = authService;
        }

        @Override
        public String getName() {
            return "loginandpassword";
        }

        public AuthFlow.LoginAndPasswordResult getResult() {
            String rawLogin = login.getText();
            String rawPassword = password.getText();
            return new AuthFlow.LoginAndPasswordResult(rawLogin, authService.makePassword(rawPassword));
        }

        @Override
        protected void doInit() {
            login = LookupHelper.lookup(layout, "#login");
            password = LookupHelper.lookup(layout, "#password");

            login.textProperty().addListener(l -> accessor.getAuthButton().setState(login.getText().isEmpty()
                    ? LoginAuthButtonComponent.AuthButtonState.UNACTIVE
                    : LoginAuthButtonComponent.AuthButtonState.ACTIVE));

            if (runtimeSettings.login != null) {
                login.setText(runtimeSettings.login);
                accessor.getAuthButton().setState(LoginAuthButtonComponent.AuthButtonState.ACTIVE);
            } else {
                accessor.getAuthButton().setState(LoginAuthButtonComponent.AuthButtonState.UNACTIVE);
            }
            if (runtimeSettings.password != null) {
                password.getStyleClass().add("hasSaved");
                password.setPromptText(launchService.getTranslation("runtime.scenes.login.password.saved"));
            }
        }

        @Override
        protected void doPostInit() {

        }


        @Override
        public void reset() {
            if (password == null) return;
            password.getStyleClass().removeAll("hasSaved");
            password.setPromptText(launchService.getTranslation("runtime.scenes.login.password"));
            password.setText("");
            login.setText("");
        }

        @Override
        public void disable() {

        }

        @Override
        public void enable() {

        }
    }
}
