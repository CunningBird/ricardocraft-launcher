package ru.ricardocraft.client.overlays;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Labeled;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.dto.request.WebSocketEvent;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.impl.ContextHelper;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.stage.AbstractStage;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.util.function.Consumer;

@Component
@Scope("prototype")
public class ProcessingOverlay extends AbstractOverlay {
    private Labeled description;

    private final RequestService service;

    public ProcessingOverlay(GuiModuleConfig guiModuleConfig, RequestService service, LaunchService launchService) {
        super("overlay/processing/processing.fxml", JavaFXApplication.getInstance(), guiModuleConfig, launchService);
        this.service = service;
    }

    @Override
    public String getName() {
        return "processing";
    }

    @Override
    protected void doInit() {
        // spinner = LookupHelper.lookup(pane, "#spinner"); //TODO: DrLeonardo?
        description = LookupHelper.lookup(layout, "#description");
    }

    @Override
    public void reset() {
        description.textProperty().unbind();
        description.getStyleClass().remove("error");
        description.setText("...");
    }

    public void errorHandle(Throwable e) {
        super.errorHandle(e);
        description.textProperty().unbind();
        description.getStyleClass().add("error");
        description.setText(e.toString());
    }

    public final <T extends WebSocketEvent> void processRequest(AbstractStage stage, String message, Request<T> request,
                                                                Consumer<T> onSuccess, EventHandler<ActionEvent> onError) {
        processRequest(stage, message, request, onSuccess, null, onError);
    }

    public final <T extends WebSocketEvent> void processRequest(AbstractStage stage, String message, Request<T> request,
                                                                Consumer<T> onSuccess, Consumer<Throwable> onException, EventHandler<ActionEvent> onError) {
        try {
            show(stage, (e) -> {
                try {
                    description.setText(message);
                    service.request(request).thenAccept((result) -> {
                        LogHelper.dev("RequestFuture complete normally");
                        onSuccess.accept(result);
                        hide(0, null);
                    }).exceptionally((error) -> {
                        if (onException != null) onException.accept(error);
                        else ContextHelper.runInFxThreadStatic(() -> errorHandle(error.getCause()));
                        hide(2500, onError);
                        return null;
                    });
                } catch (IOException ex) {
                    errorHandle(ex);
                    hide(2500, onError);
                }
            });
        } catch (Exception e) {
            errorHandle(e);
        }
    }
}
