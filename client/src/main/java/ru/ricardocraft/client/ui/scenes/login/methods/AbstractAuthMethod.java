package ru.ricardocraft.client.ui.scenes.login.methods;

import ru.ricardocraft.client.dto.response.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.client.ui.scenes.login.AuthFlow;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractAuthMethod<T extends GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> {
    public abstract void prepare();

    public abstract void reset();

    public abstract CompletableFuture<Void> show(T details);

    public abstract CompletableFuture<AuthFlow.LoginAndPasswordResult> auth(T details);

    public abstract void onAuthClicked();

    public abstract void onUserCancel();

    public abstract CompletableFuture<Void> hide();

    public abstract boolean isOverlay();

    public static class UserAuthCanceledException extends RuntimeException {
        public UserAuthCanceledException() {
        }

    }
}
