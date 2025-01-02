package ru.ricardocraft.client.service.modules;

public interface LauncherModulesManager {

    LauncherModule loadModule(LauncherModule module);

    LauncherModule getModule(String name);

    /**
     * Invoke event processing for all modules.
     * Event processing is carried out in the order of the modules in the list (sorted by priority)
     *
     * @param event event handled
     * @param <T>   event type
     */
    <T extends LauncherModule.Event> void invokeEvent(T event);
}
