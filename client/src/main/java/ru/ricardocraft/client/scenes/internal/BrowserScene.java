package ru.ricardocraft.client.scenes.internal;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.scenes.AbstractScene;

public class BrowserScene extends AbstractScene {
    private TextField address;
    private Button browseButton;
    private StackPane stackPane;
    private WebView webView;
    public BrowserScene(JavaFXApplication application) {
        super("scenes/internal/browser/browser.fxml", application);
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
