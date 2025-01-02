package ru.ricardocraft.client.ui.scenes.internal;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.ui.scenes.AbstractScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;

public abstract class BrowserScene extends AbstractScene {
    private TextField address;
    private Button browseButton;
    private StackPane stackPane;
    private WebView webView;

    public BrowserScene(LauncherConfig config,
                        GuiModuleConfig guiModuleConfig,
                        AuthService authService,
                        LaunchService launchService,
                        SettingsManager settingsManager) {
        super("scenes/internal/browser/browser.fxml", config, guiModuleConfig, authService, launchService, settingsManager);
    }

    @Override
    public String getName() {
        return "browser";
    }

    @Override
    protected void doInit() {
        stackPane = LookupHelper.lookup(layout, "#content");
        webView = new WebView();
        stackPane.getChildren().add(webView);
        browseButton = LookupHelper.lookup(layout, "#browse");
        address = LookupHelper.lookup(layout, "#address");
        browseButton.setOnAction((e) -> webView.getEngine().load(address.getText()));
    }

    @Override
    public void reset() {

    }
}
