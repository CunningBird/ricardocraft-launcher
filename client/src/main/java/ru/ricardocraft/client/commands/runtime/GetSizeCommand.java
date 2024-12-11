package ru.ricardocraft.client.commands.runtime;

import javafx.scene.layout.Pane;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.impl.AbstractStage;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class GetSizeCommand extends Command {
    private final JavaFXApplication application;

    public GetSizeCommand(JavaFXApplication application) {
        this.application = application;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return null;
    }

    @Override
    public void invoke(String... args) throws Exception {
        AbstractStage abstractStage = application.getMainStage();
        var stage = abstractStage.getStage();
        LogHelper.info("Stage: H: %f W: %f", stage.getHeight(), stage.getWidth());
        var scene = stage.getScene();
        LogHelper.info("Scene: H: %f W: %f", scene.getHeight(), scene.getWidth());
        var stackPane = (Pane)scene.getRoot();
        LogHelper.info("StackPane: H: %f W: %f", stackPane.getHeight(), stackPane.getWidth());
        var layout = (Pane)stackPane.getChildren().get(0);
        LogHelper.info("Layout: H: %f W: %f", layout.getHeight(), layout.getWidth());
    }
}
