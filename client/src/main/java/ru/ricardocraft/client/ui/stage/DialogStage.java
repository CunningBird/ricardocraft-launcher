package ru.ricardocraft.client.ui.stage;

import javafx.event.Event;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import ru.ricardocraft.client.ui.DesignConstants;
import ru.ricardocraft.client.ui.dialogs.AbstractDialog;
import ru.ricardocraft.client.base.helper.EnFSHelper;
import ru.ricardocraft.client.base.helper.LogHelper;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.ui.impl.AbstractVisualComponent;

import java.io.IOException;

public abstract class DialogStage extends AbstractStage {
    public DialogStage(String title, AbstractDialog dialog) throws Exception {
        super(newStage());
        stage.setTitle(title);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(false);
        stage.setOnCloseRequest(Event::consume);
        scene.setFill(Color.TRANSPARENT);
        // Icons
        try {
            Image icon = new Image(EnFSHelper.getResourceURL("favicon.png").toString());
            stage.getIcons().add(icon);
        } catch (IOException e) {
            LogHelper.error(e);
        }
        setClipRadius(DesignConstants.SCENE_CLIP_RADIUS, DesignConstants.SCENE_CLIP_RADIUS);
        setScene(dialog, true);
        enableMouseDrag(dialog.getLayout());
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        if (bounds.getMaxX() == 0 || bounds.getMaxY() == 0) {
            bounds = screen.getBounds();
        }
        LogHelper.info("Bounds: X: %f Y: %f", bounds.getMaxX(), bounds.getMaxY());
        LookupHelper.Point2D coords = dialog.getOutSceneCoords(bounds);
        stage.setX(coords.x);
        stage.setY(coords.y);
    }

    abstract protected AbstractVisualComponent getByName(String name);
}
