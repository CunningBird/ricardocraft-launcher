package ru.ricardocraft.client.scenes.login.methods;

import ru.ricardocraft.client.dto.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.client.dto.request.auth.password.AuthCodePassword;
import ru.ricardocraft.client.helper.LogHelper;
import ru.ricardocraft.client.scenes.login.AuthFlow;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.scenes.login.WebAuthOverlay;

import java.util.concurrent.CompletableFuture;

public abstract class WebAuthMethod extends AbstractAuthMethod<AuthWebViewDetails> {

    private final WebAuthOverlay overlay;
    private final LoginScene.LoginSceneAccessor accessor;

    public WebAuthMethod(LoginScene.LoginSceneAccessor accessor) {
        this.accessor = accessor;
        this.overlay = getWebAuthOverlay();
    }

    abstract protected WebAuthOverlay getWebAuthOverlay();

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
