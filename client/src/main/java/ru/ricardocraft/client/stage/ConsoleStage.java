package ru.ricardocraft.client.stage;

import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import ru.ricardocraft.client.config.DesignConstants;
import ru.ricardocraft.client.impl.AbstractStage;
import ru.ricardocraft.client.JavaFXApplication;

public class ConsoleStage extends AbstractStage {
    public ConsoleStage(JavaFXApplication application) {
        super(application, application.newStage());
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("%s Launcher Console".formatted(application.config.projectName));
        stage.setResizable(false);
        setClipRadius(DesignConstants.SCENE_CLIP_RADIUS, DesignConstants.SCENE_CLIP_RADIUS);
    }
}
