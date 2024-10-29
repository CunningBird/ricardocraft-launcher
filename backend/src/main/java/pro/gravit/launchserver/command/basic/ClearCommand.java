package pro.gravit.launchserver.command.basic;

import pro.gravit.launchserver.command.utls.Command;
import pro.gravit.launchserver.command.utls.CommandHandler;
import pro.gravit.launchserver.helper.LogHelper;

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
