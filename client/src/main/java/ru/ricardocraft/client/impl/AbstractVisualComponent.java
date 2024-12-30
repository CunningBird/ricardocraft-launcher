package ru.ricardocraft.client.impl;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.dto.request.RequestException;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.helper.EnFSHelper;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.stage.AbstractStage;
import ru.ricardocraft.client.helper.LogHelper;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public abstract class AbstractVisualComponent {

    protected final JavaFXApplication application;

    protected final LaunchService launchService;

    private final String sysFxmlPath;
    protected final ContextHelper contextHelper;
    protected final FXExecutorService fxExecutor;
    public AbstractStage currentStage;
    protected Pane layout;
    private Parent sysFxmlRoot;
    private CompletableFuture<Node> sysFxmlFuture;
    boolean isInit;
    boolean isPostInit;
    public boolean isResetOnShow = false;

    protected AbstractVisualComponent(String fxmlPath,
                                      JavaFXApplication application,
                                      GuiModuleConfig guiModuleConfig,
                                      LaunchService launchService) {
        this.application = application;
        this.launchService = launchService;
        this.sysFxmlPath = fxmlPath;
        this.contextHelper = new ContextHelper(this);
        this.fxExecutor = new FXExecutorService(contextHelper);
        if (guiModuleConfig.lazy) {
            this.sysFxmlFuture = launchService.fxmlFactory.getAsync(sysFxmlPath);
        }
    }

    public static FadeTransition fade(Node region, double delay, double from, double to,
            EventHandler<ActionEvent> onFinished) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(100), region);
        if (onFinished != null) fadeTransition.setOnFinished(onFinished);
        fadeTransition.setDelay(Duration.millis(delay));
        fadeTransition.setFromValue(from);
        fadeTransition.setToValue(to);
        fadeTransition.play();
        return fadeTransition;
    }

    protected void initBasicControls(Parent header) {
        if (header == null) {
            LogHelper.warning("Scene %s header button(#close, #hide) deprecated", getName());
            LookupHelper.<ButtonBase>lookupIfPossible(layout, "#close")
                        .ifPresent((b) -> b.setOnAction((e) -> currentStage.close()));
            LookupHelper.<ButtonBase>lookupIfPossible(layout, "#hide")
                        .ifPresent((b) -> b.setOnAction((e) -> currentStage.hide()));
        } else {
            LookupHelper.<ButtonBase>lookupIfPossible(header, "#controls", "#exit")
                        .ifPresent((b) -> b.setOnAction((e) -> currentStage.close()));
            LookupHelper.<ButtonBase>lookupIfPossible(header, "#controls", "#minimize")
                        .ifPresent((b) -> b.setOnAction((e) -> currentStage.hide()));
        }
        currentStage.enableMouseDrag(layout);
    }

    public Pane getLayout() {
        return layout;
    }

    public boolean isInit() {
        return isInit;
    }

    public abstract String getName();

    public synchronized Parent getFxmlRoot() {
        try {
            if (sysFxmlRoot == null) {
                if (sysFxmlFuture == null) {
                    this.sysFxmlFuture = launchService.fxmlFactory.getAsync(sysFxmlPath);
                }
                sysFxmlRoot = (Parent) sysFxmlFuture.get();
            }
            return sysFxmlRoot;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CompletionException) {
                cause = cause.getCause();
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new FXMLFactory.FXMLLoadException(cause);
        }
    }

    public void init() throws Exception {
        if (layout == null) {
            layout = (Pane) getFxmlRoot();
        }
        doInit();
        isInit = true;
    }

    public void postInit() {
        if (!isPostInit) {
            doPostInit();
            isPostInit = true;
        }
    }

    public static URL tryResource(String name) {
        try {
            return EnFSHelper.getResourceURL(name);
        } catch (IOException e) {
            return null;
        }
    }

    protected abstract void doInit();

    protected abstract void doPostInit();

    public abstract void reset();

    public abstract void disable();

    public abstract void enable();

    public void errorHandle(Throwable e) {
        String message = null;
        if (e instanceof CompletionException) {
            e = e.getCause();
        }
        if (e instanceof ExecutionException) {
            e = e.getCause();
        }
        if (e instanceof RequestException) {
            message = e.getMessage();
        }
        if (message == null) {
            message = "%s: %s".formatted(e.getClass().getName(), e.getMessage());
        } else {
            message = launchService.getTranslation("runtime.request.".concat(message), message);
        }
        LogHelper.error(e);
        launchService.createNotification("Error", message);
    }

    public Parent getFxmlRootPrivate() {
        return getFxmlRoot();
    }

    public boolean isDisableReturnBack() {
        return false;
    }
}
