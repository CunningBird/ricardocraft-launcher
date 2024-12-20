package pro.gravit.launcher.gui.runtime.console;

import pro.gravit.launcher.gui.base.request.management.GetConnectUUIDRequest;
import pro.gravit.launcher.gui.utils.command.Command;
import pro.gravit.launcher.gui.utils.helper.LogHelper;

public class GetConnectUUIDCommand extends Command {
    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Get your connectUUID";
    }

    @Override
    public void invoke(String... args) throws Exception {
        var response = new GetConnectUUIDRequest().request();
        LogHelper.info("Your connectUUID: %s | shardId %d", response.connectUUID.toString(), response.shardId);
    }
}
