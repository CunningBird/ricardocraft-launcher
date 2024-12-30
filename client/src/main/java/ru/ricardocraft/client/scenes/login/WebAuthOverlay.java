package ru.ricardocraft.client.scenes.login;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.overlays.AbstractOverlay;
import ru.ricardocraft.client.scenes.login.methods.AbstractAuthMethod;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.helper.LogHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
@Scope("prototype")
public class WebAuthOverlay extends AbstractOverlay {

    private WebView webView;
    public CompletableFuture<AuthFlow.LoginAndPasswordResult> future;

    public WebAuthOverlay(GuiModuleConfig guiModuleConfig, LaunchService launchService) {
        super("overlay/webauth/webauth.fxml", JavaFXApplication.getInstance(), guiModuleConfig, launchService);
    }

    @Override
    public String getName() {
        return "webView";
    }

    public void hide(EventHandler<ActionEvent> onFinished) {
        hide(10, onFinished);
    }

    @Override
    protected void doInit() {
        ScrollPane webViewPane = LookupHelper.lookup(layout, "#webview");
        webView = new WebView();
        webViewPane.setContent(new VBox(webView));
        LookupHelper.<Button>lookup(layout, "#exit").setOnAction((e) -> {
            if (future != null) {
                future.completeExceptionally(new AbstractAuthMethod.UserAuthCanceledException());
            }
            hide(null);
        });
    }

    public void follow(String url, String redirectUrl, Consumer<String> redirectCallback) {
        LogHelper.dev("Load url %s", url);
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().load(url);
        if (redirectCallback != null) {
            webView.getEngine().locationProperty().addListener((obs, oldLocation, newLocation) -> {
                if (newLocation != null) {
                    LogHelper.dev("Location: %s", newLocation);
                    if (redirectUrl != null) {
                        if (newLocation.startsWith(redirectUrl)) {
                            redirectCallback.accept(newLocation);
                        }
                    } else {
                        redirectCallback.accept(newLocation);
                    }
                }
            });
        }
    }

    @Override
    public void reset() {

    }
}