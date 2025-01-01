package ru.ricardocraft.client.scenes.console;

import javafx.event.ActionEvent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.commands.CommandHandler;
import ru.ricardocraft.client.helper.LogHelper;

public abstract class ConsoleScene extends AbstractScene {
    private static final long MAX_LENGTH = 16384;
    private static final int REMOVE_LENGTH = 1024;
    private TextField commandLine;
    private TextArea output;

    private final CommandHandler commandHandler;

    public ConsoleScene(LauncherConfig config,
                        GuiModuleConfig guiModuleConfig,
                        AuthService authService,
                        LaunchService launchService,
                        CommandHandler commandHandler,
                        SettingsManager settingsManager) {
        super("scenes/console/console.fxml", config, guiModuleConfig, authService, launchService, settingsManager);
        this.commandHandler = commandHandler;
    }

    @Override
    protected void doInit() {
        output = LookupHelper.lookup(layout, "#output");
        commandLine = LookupHelper.lookup(layout, "#commandInput");
        LogHelper.addOutput(this::append, LogHelper.OutputTypes.PLAIN);
        commandLine.setOnAction(this::send);
        LookupHelper.<ButtonBase>lookup(layout, "#send").setOnAction(this::send);
    }

    @Override
    public void reset() {
        output.clear();
        commandLine.clear();
        commandLine.getStyleClass().removeAll("InputError");
    }

    @Override
    public String getName() {
        return "console";
    }

    private void send(ActionEvent ignored) {
        String command = commandLine.getText();
        commandLine.clear();
        try {
            commandHandler.evalNative(command, false);
            commandLine.getStyleClass().removeAll("InputError");
        } catch (Exception ex) {
            LogHelper.error(ex);
            commandLine.getStyleClass().add("InputError");
        }
    }

    private void append(String text) {
        contextHelper.runInFxThread(() -> {
            if (output.lengthProperty().get() > MAX_LENGTH) output.deleteText(0, REMOVE_LENGTH);
            output.appendText(text.concat("\n"));
        });
    }
}
