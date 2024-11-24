package ru.ricardocraft.client.commands.runtime;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.utils.command.Command;

public class WebViewCommand extends Command {
    private final JavaFXApplication application;

    public WebViewCommand(JavaFXApplication application) {
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

    }
}
