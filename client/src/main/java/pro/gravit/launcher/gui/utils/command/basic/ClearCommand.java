package pro.gravit.launcher.gui.utils.command.basic;

import pro.gravit.launcher.gui.utils.command.Command;
import pro.gravit.launcher.gui.utils.command.CommandHandler;
import pro.gravit.launcher.gui.utils.helper.LogHelper;

public final class ClearCommand extends Command {
    private final CommandHandler handler;

    public ClearCommand(CommandHandler handler) {
        this.handler = handler;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Clear terminal";
    }

    @Override
    public void invoke(String... args) throws Exception {
        handler.clear();
        LogHelper.subInfo("Terminal cleared");
    }
}
