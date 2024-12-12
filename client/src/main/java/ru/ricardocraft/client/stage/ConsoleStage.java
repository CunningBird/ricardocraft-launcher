package ru.ricardocraft.client.stage;

import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.DesignConstants;
import ru.ricardocraft.client.config.LauncherConfig;

public class ConsoleStage extends AbstractStage {
    public ConsoleStage(JavaFXApplication application, LauncherConfig config) {
        super(application.gui, newStage());
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("%s Launcher Console".formatted(config.projectName));
        stage.setResizable(false);
        setClipRadius(DesignConstants.SCENE_CLIP_RADIUS, DesignConstants.SCENE_CLIP_RADIUS);
    }
}
