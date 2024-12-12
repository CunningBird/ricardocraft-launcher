package ru.ricardocraft.client.scenes.login.methods;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.client.base.request.auth.password.AuthCodePassword;
import ru.ricardocraft.client.scenes.login.AuthFlow;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.scenes.login.WebAuthOverlay;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.util.concurrent.CompletableFuture;

public class WebAuthMethod extends AbstractAuthMethod<AuthWebViewDetails> {

    private final WebAuthOverlay overlay;
    private final LoginScene.LoginSceneAccessor accessor;

    public WebAuthMethod(LoginScene.LoginSceneAccessor accessor) {
        JavaFXApplication application = accessor.getApplication();

        this.accessor = accessor;
        this.overlay = (WebAuthOverlay) application.gui.getByName("webView");
    }

    @Override
    public void prepare() {

    }

    @Override
    public void reset() {
        overlay.reset();
    }

    @Override
    public CompletableFuture<Void> show(AuthWebViewDetails details) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            accessor.showOverlay(overlay, (e) -> future.complete(null));
        } catch (Exception e) {
            accessor.errorHandle(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<AuthFlow.LoginAndPasswordResult> auth(AuthWebViewDetails details) {
        overlay.future = new CompletableFuture<>();
        overlay.follow(details.url, details.redirectUrl, (r) -> {
            LogHelper.dev("Redirect uri: %s", r);
            overlay.future.complete(new AuthFlow.LoginAndPasswordResult(null, new AuthCodePassword(r)));
        });
        return overlay.future;
    }

    @Override
    public void onAuthClicked() {
    }

    @Override
    public void onUserCancel() {

    }

    @Override
    public CompletableFuture<Void> hide() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        overlay.hide((r) -> future.complete(null));
        return future;
    }

    @Override
    public boolean isOverlay() {
        return true;
    }
}
