package ru.ricardocraft.client.dialogs;

import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.service.LaunchService;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDialog extends AbstractVisualComponent {
    private final List<ContextHelper.GuiExceptionRunnable> onClose = new ArrayList<>(1);

    protected AbstractDialog(String fxmlPath,
                             JavaFXApplication application,
                             GuiModuleConfig guiModuleConfig,
                             LaunchService launchService) {
        super(fxmlPath, application, guiModuleConfig, launchService);
    }

    @Override
    protected void doPostInit() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void disable() {

    }

    @Override
    public void enable() {

    }

    public void setOnClose(ContextHelper.GuiExceptionRunnable callback) {
        onClose.add(callback);
    }

    public void close() throws Throwable {
        for (ContextHelper.GuiExceptionRunnable callback : onClose) {
            callback.call();
        }
    }

    public LookupHelper.Point2D getOutSceneCoords(Rectangle2D bounds) {

        return new LookupHelper.Point2D((bounds.getMaxX() - layout.getPrefWidth()) / 2.0,
                (bounds.getMaxY() - layout.getPrefHeight()) / 2.0);
    }

    public LookupHelper.Point2D getSceneCoords(Pane root) {

        return new LookupHelper.Point2D((root.getPrefWidth() - layout.getPrefWidth()) / 2.0,
                (root.getPrefHeight() - layout.getPrefHeight()) / 2.0);
    }
}
