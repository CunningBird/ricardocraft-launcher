package ru.ricardocraft.client.commands.runtime;

import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.overlays.AbstractOverlay;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.stage.PrimaryStage;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.utils.command.Command;

public class WarpCommand extends Command {
    private JavaFXApplication application;

    public WarpCommand(JavaFXApplication application) {
        this.application = application;
    }

    @Override
    public String getArgsDescription() {
        return "[scene/overlay] [name]";
    }

    @Override
    public String getUsageDescription() {
        return "warp to any scene/overlay";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 2);
        if (application == null) application = JavaFXApplication.getInstance();
        if (args[0].equals("scene")) {
            AbstractScene scene = (AbstractScene) application.gui.getByName(args[1]);
            if (scene == null) throw new IllegalArgumentException("Scene %s not found".formatted(args[1]));
            PrimaryStage stage = application.getMainStage();
            ContextHelper.runInFxThreadStatic(() -> {
                stage.setScene(scene, true);
                if (!stage.isShowing()) {
                    stage.show();
                }
            });
        } else if (args[0].equals("overlay")) {
            AbstractOverlay overlay = (AbstractOverlay) application.gui.getByName(args[1]);
            if (overlay == null) throw new IllegalArgumentException("Overlay %s not found".formatted(args[1]));
            PrimaryStage stage = application.getMainStage();
            if (stage.isNullScene()) throw new IllegalStateException("Please wrap to scene before");
            AbstractScene scene = (AbstractScene) stage.getVisualComponent();
            ContextHelper.runInFxThreadStatic(() -> scene.showOverlay(overlay, e -> {
            }));
        } else throw new IllegalArgumentException("%s not found".formatted(args[0]));
    }
}
