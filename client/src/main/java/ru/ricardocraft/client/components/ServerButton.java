package ru.ricardocraft.client.components;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.impl.AbstractVisualComponent;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;
import ru.ricardocraft.client.utils.JavaFxUtils;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.net.URL;
import java.util.concurrent.atomic.AtomicLong;

public class ServerButton extends AbstractVisualComponent {
    private static final String SERVER_BUTTON_FXML = "components/serverButton.fxml";
    private static final String SERVER_BUTTON_CUSTOM_FXML = "components/serverButton/%s.fxml";
    private static final String SERVER_BUTTON_DEFAULT_IMAGE = "images/servers/example.png";
    private static final String SERVER_BUTTON_CUSTOM_IMAGE = "images/servers/%s.png";
    public ClientProfile profile;
    private Button saveButton;
    private Button resetButton;
    private Region serverLogo;

    private final PingService pingService;

    protected ServerButton(JavaFXApplication application,
                           GuiModuleConfig guiModuleConfig,
                           LaunchService launchService,
                           PingService pingService,
                           ClientProfile profile) {
        super(getServerButtonFxml(application, profile), application, guiModuleConfig, launchService);
        this.profile = profile;
        this.pingService = pingService;
    }

    public static ServerButton createServerButton(JavaFXApplication application,
                                                  GuiModuleConfig guiModuleConfig,
                                                  LaunchService launchService,
                                                  PingService pingService,
                                                  ClientProfile profile) {
        return new ServerButton(application, guiModuleConfig, launchService, pingService, profile);
    }

    private static String getServerButtonFxml(JavaFXApplication application, ClientProfile profile) {
        String customFxml = String.format(SERVER_BUTTON_CUSTOM_FXML, profile.getUUID().toString());
        URL fxml = tryResource(customFxml);
        if (fxml != null) {
            return customFxml;
        }
        return SERVER_BUTTON_FXML;
    }

    @Override
    public String getName() {
        return "serverButton";
    }

    @Override
    protected void doInit() {
        LookupHelper.<Labeled>lookup(layout, "#nameServer").setText(profile.getTitle());
        LookupHelper.<Labeled>lookup(layout, "#genreServer").setText(profile.getVersion().toString());
        this.serverLogo = LookupHelper.lookup(layout, "#serverLogo");
        URL logo = tryResource(String.format(SERVER_BUTTON_CUSTOM_IMAGE, profile.getUUID().toString()));
        if (logo == null) {
            logo = tryResource(SERVER_BUTTON_DEFAULT_IMAGE);
        }
        if (logo != null) {
            this.serverLogo.setBackground(new Background(new BackgroundImage(new Image(logo.toString()),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, new BackgroundSize(0.0, 0.0, true, true, false, true))));
            JavaFxUtils.setRadius(this.serverLogo, 20.0);
        }
        AtomicLong currentOnline = new AtomicLong(0);
        AtomicLong maxOnline = new AtomicLong(0);
        Runnable update = () -> contextHelper.runInFxThread(() -> {
            if (currentOnline.get() == 0 && maxOnline.get() == 0) {
                LookupHelper.<Labeled>lookup(layout, "#online").setText("?");
            } else {
                LookupHelper.<Labeled>lookup(layout, "#online").setText(String.valueOf(currentOnline.get()));
            }
        });
        for (ClientProfile.ServerProfile serverProfile : profile.getServers()) {
            pingService.getPingReport(serverProfile.name).thenAccept((report) -> {
                if (report != null) {
                    currentOnline.addAndGet(report.playersOnline);
                    maxOnline.addAndGet(report.maxPlayers);
                }
                update.run();
            });
        }
        saveButton = LookupHelper.lookup(layout, "#save");
        resetButton = LookupHelper.lookup(layout, "#reset");
    }

    @Override
    protected void doPostInit() {

    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> eventHandler) {
        layout.setOnMouseClicked(eventHandler);
    }

    public void enableSaveButton(String text, EventHandler<ActionEvent> eventHandler) {
        saveButton.setVisible(true);
        if (text != null) saveButton.setText(text);
        saveButton.setOnAction(eventHandler);
    }

    public void enableResetButton(String text, EventHandler<ActionEvent> eventHandler) {
        resetButton.setVisible(true);
        if (text != null) resetButton.setText(text);
        resetButton.setOnAction(eventHandler);
    }

    public void addTo(Pane pane) {
        if (!isInit()) {
            try {
                init();
            } catch (Exception e) {
                LogHelper.error(e);
            }
        }
        pane.getChildren().add(layout);
    }

    public void addTo(Pane pane, int position) {
        if (!isInit()) {
            try {
                init();
            } catch (Exception e) {
                LogHelper.error(e);
            }
        }
        pane.getChildren().add(position, layout);
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
}
