package ru.ricardocraft.client.impl;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.service.LaunchService;

@Component
@Scope("prototype")
public class BackgroundComponent extends AbstractVisualComponent {

    public BackgroundComponent(GuiModuleConfig guiModuleConfig, LaunchService launchService) {
        super("components/background.fxml", JavaFXApplication.getInstance(), guiModuleConfig, launchService);
    }

    @Override
    public String getName() {
        return "background";
    }

    @Override
    protected void doInit() {

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
}
