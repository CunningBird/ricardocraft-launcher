package ru.ricardocraft.client.ui.scenes.login.methods;

import javafx.scene.control.TextField;
import ru.ricardocraft.client.dto.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.ui.impl.AbstractVisualComponent;
import ru.ricardocraft.client.ui.impl.ContextHelper;
import ru.ricardocraft.client.ui.scenes.login.AuthFlow;
import ru.ricardocraft.client.ui.scenes.login.LoginAuthButtonComponent;
import ru.ricardocraft.client.ui.scenes.login.LoginScene;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.base.helper.LogHelper;

import java.util.concurrent.CompletableFuture;

public class LoginOnlyAuthMethod extends AbstractAuthMethod<AuthLoginOnlyDetails> {
    private final LoginOnlyOverlay overlay;
    private final RuntimeSettings runtimeSettings;
    private final LoginScene.LoginSceneAccessor accessor;

    public LoginOnlyAuthMethod(LoginScene.LoginSceneAccessor accessor,
                               RuntimeSettings runtimeSettings,
                               GuiModuleConfig guiModuleConfig,
                               LaunchService launchService) {
        this.accessor = accessor;
        this.runtimeSettings = runtimeSettings;
        this.overlay = new LoginOnlyOverlay(guiModuleConfig, launchService);
    }

    @Override
    public void prepare() {
    }

    @Override
    public void reset() {
        overlay.reset();
    }

    @Override
    public CompletableFuture<Void> show(AuthLoginOnlyDetails details) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
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
    public CompletableFuture<AuthFlow.LoginAndPasswordResult> auth(AuthLoginOnlyDetails details) {
        overlay.future = new CompletableFuture<>();
        String login = overlay.login.getText();
        if (login != null && !login.isEmpty()) {
            return CompletableFuture.completedFuture(new AuthFlow.LoginAndPasswordResult(login, null));
        }
        return overlay.future;
    }

    @Override
    public void onAuthClicked() {
        overlay.future.complete(overlay.getResult());
    }

    @Override
    public void onUserCancel() {
        overlay.future.completeExceptionally(LoginOnlyOverlay.USER_AUTH_CANCELED_EXCEPTION);
    }

    @Override
    public CompletableFuture<Void> hide() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isOverlay() {
        return false;
    }

    public class LoginOnlyOverlay extends AbstractVisualComponent {
        private static final UserAuthCanceledException USER_AUTH_CANCELED_EXCEPTION = new UserAuthCanceledException();
        private TextField login;
        private CompletableFuture<AuthFlow.LoginAndPasswordResult> future;

        public LoginOnlyOverlay(GuiModuleConfig guiModuleConfig, LaunchService launchService) {
            super("scenes/login/methods/loginonly.fxml", guiModuleConfig, launchService);
        }

        @Override
        public String getName() {
            return "loginonly";
        }

        public AuthFlow.LoginAndPasswordResult getResult() {
            String rawLogin = login.getText();
            return new AuthFlow.LoginAndPasswordResult(rawLogin, null);
        }

        @Override
        protected void doInit() {
            login = LookupHelper.lookup(layout, "#login");
            login.textProperty().addListener(l -> accessor.getAuthButton().setState(login.getText().isEmpty()
                                                                                            ? LoginAuthButtonComponent.AuthButtonState.UNACTIVE
                                                                                            : LoginAuthButtonComponent.AuthButtonState.ACTIVE));
            if (runtimeSettings.login != null) {
                login.setText(runtimeSettings.login);
                accessor.getAuthButton().setState(LoginAuthButtonComponent.AuthButtonState.ACTIVE);
            } else {
                accessor.getAuthButton().setState(LoginAuthButtonComponent.AuthButtonState.UNACTIVE);
            }
        }

        @Override
        protected void doPostInit() {

        }


        @Override
        public void reset() {
            if(login == null) return;
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