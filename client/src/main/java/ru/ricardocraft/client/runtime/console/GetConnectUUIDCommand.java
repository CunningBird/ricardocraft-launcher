package ru.ricardocraft.client.runtime.console;

import ru.ricardocraft.client.base.request.management.GetConnectUUIDRequest;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.helper.LogHelper;

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