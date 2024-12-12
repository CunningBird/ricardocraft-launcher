package ru.ricardocraft.client.overlays;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.stage.AbstractStage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractOverlay extends AbstractVisualComponent {
    private final AtomicInteger useCounter = new AtomicInteger(0);
    private final AtomicReference<FadeTransition> fadeTransition = new AtomicReference<>();

    protected AbstractOverlay(String fxmlPath,
                              JavaFXApplication application,
                              GuiModuleConfig guiModuleConfig,
                              LaunchService launchService) {
        super(fxmlPath, application, guiModuleConfig, launchService);
    }

    public final void init() throws Exception {
        super.init();
    }

    public final void hide(double delay, EventHandler<ActionEvent> onFinished) {
        if (useCounter.decrementAndGet() != 0) {
            contextHelper.runInFxThread(() -> {
                if (onFinished != null) {
                    onFinished.handle(null);
                }
            });
            return;
        }
        if (!isInit()) throw new IllegalStateException("Using method hide before init");
        fadeTransition.set(fade(getFxmlRoot(), delay, 1.0, 0.0, (f) -> {
            if (onFinished != null) {
                onFinished.handle(f);
            }
            currentStage.pull(getFxmlRoot());
            currentStage.enable();
            fadeTransition.set(null);
        }));
    }

    protected abstract void doInit();

    @Override
    protected void doPostInit() {

    }

    public abstract void reset();

    public void disable() {
    }

    public void enable() {
    }

    public void show(AbstractStage stage, EventHandler<ActionEvent> onFinished) throws Exception {
        if (!isInit()) {
            init();
        }
        if (useCounter.incrementAndGet() != 1) {
            contextHelper.runInFxThread(() -> {
                if (onFinished != null) {
                    onFinished.handle(null);
                }
            });
            return;
        }
        if (fadeTransition.get() != null) {
            currentStage.disable();
            fadeTransition.get().jumpTo(Duration.ZERO);
            fadeTransition.get().stop();
            contextHelper.runInFxThread(() -> {
                if (onFinished != null) {
                    onFinished.handle(null);
                }
            });
            fadeTransition.set(null);
            return;
        }
        Node root = getFxmlRoot();
        this.currentStage = stage;
        currentStage.enableMouseDrag(layout);
        currentStage.push(root);
        currentStage.disable();
        fade(root, 100, 0.0, 1.0, (f) -> {
            if (onFinished != null) {
                onFinished.handle(f);
            }
        });
    }
}
