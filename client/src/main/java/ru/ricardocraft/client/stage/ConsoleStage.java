package ru.ricardocraft.client.stage;

import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import ru.ricardocraft.client.config.DesignConstants;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.impl.AbstractVisualComponent;

public abstract class ConsoleStage extends AbstractStage {

    public ConsoleStage(LauncherConfig config) {
        super(newStage());
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);
        stage.setTitle("%s Launcher Console".formatted(config.projectName));
        stage.setResizable(false);
        setClipRadius(DesignConstants.SCENE_CLIP_RADIUS, DesignConstants.SCENE_CLIP_RADIUS);
    }

    abstract protected AbstractVisualComponent getByName(String name);
}
