package pro.gravit.launcher.gui.client.events;

import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.utils.command.CommandHandler;

public class ClientUnlockConsoleEvent extends LauncherModule.Event {
    public final CommandHandler handler;

    public ClientUnlockConsoleEvent(CommandHandler handler) {
        this.handler = handler;
    }
}
