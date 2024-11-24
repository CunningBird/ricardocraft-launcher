package ru.ricardocraft.client.client.events;

import ru.ricardocraft.client.base.modules.LauncherModule;
import ru.ricardocraft.client.utils.command.CommandHandler;

public class ClientUnlockConsoleEvent extends LauncherModule.Event {
    public final CommandHandler handler;

    public ClientUnlockConsoleEvent(CommandHandler handler) {
        this.handler = handler;
    }
}
