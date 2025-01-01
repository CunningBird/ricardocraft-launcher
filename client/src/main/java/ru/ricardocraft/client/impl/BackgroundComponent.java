package ru.ricardocraft.client.impl;

import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.service.LaunchService;

public class BackgroundComponent extends AbstractVisualComponent {

    public BackgroundComponent(GuiModuleConfig guiModuleConfig, LaunchService launchService) {
        super("components/background.fxml", guiModuleConfig, launchService);
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
