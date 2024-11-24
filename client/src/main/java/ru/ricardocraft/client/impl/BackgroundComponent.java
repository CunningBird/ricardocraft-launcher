package ru.ricardocraft.client.impl;

import ru.ricardocraft.client.JavaFXApplication;

public class BackgroundComponent extends AbstractVisualComponent {
    public BackgroundComponent(JavaFXApplication application) {
        super("components/background.fxml", application);
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
