package ru.ricardocraft.client.overlays;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.service.LaunchService;

public abstract class CenterOverlay extends AbstractOverlay {
    private volatile Pane overrideFxmlRoot;

    public CenterOverlay(String fxmlPath, GuiModuleConfig guiModuleConfig, LaunchService launchService) {
        super(fxmlPath, guiModuleConfig, launchService);
    }

    @Override
    public synchronized Parent getFxmlRoot() {
        if (overrideFxmlRoot == null) {
            Parent fxmlRoot = super.getFxmlRoot();
            HBox hBox = new HBox();
            hBox.getChildren().add(fxmlRoot);
            hBox.setAlignment(Pos.CENTER);
            VBox vbox = new VBox();
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().add(hBox);
            overrideFxmlRoot = vbox;
        }
        return overrideFxmlRoot;
    }
}
