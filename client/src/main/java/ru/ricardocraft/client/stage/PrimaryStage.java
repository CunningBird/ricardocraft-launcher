package ru.ricardocraft.client.stage;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.ricardocraft.client.config.DesignConstants;
import ru.ricardocraft.client.helper.EnFSHelper;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.impl.GuiObjectsContainer;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;

public class PrimaryStage extends AbstractStage {

    public PrimaryStage(GuiObjectsContainer guiObjectsContainer, Stage primaryStage, String title) {
        super(guiObjectsContainer, primaryStage);
        primaryStage.setTitle(title);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
        scene.setFill(Color.TRANSPARENT);
        // Icons
        try {
            Image icon = new Image(EnFSHelper.getResourceURL("favicon.png").toString());
            stage.getIcons().add(icon);
        } catch (IOException e) {
            LogHelper.error(e);
        }
        setClipRadius(DesignConstants.SCENE_CLIP_RADIUS, DesignConstants.SCENE_CLIP_RADIUS);
    }

    public void pushBackground(AbstractVisualComponent component) {
        scenePosition.incrementAndGet();
        addBefore(visualComponent.getLayout(), component.getLayout());
    }

    public void pullBackground(AbstractVisualComponent component) {
        scenePosition.decrementAndGet();
        pull(component.getLayout());
    }

    @Override
    public void close() {
        Platform.exit();
    }
}
