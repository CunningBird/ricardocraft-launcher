package ru.ricardocraft.client.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.base.request.management.GetConnectUUIDRequest;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.command.CommandHandler;
import ru.ricardocraft.client.utils.helper.LogHelper;

@Component
public class GetConnectUUIDCommand extends Command {

    @Autowired
    public GetConnectUUIDCommand(CommandHandler commandHandler) {
        commandHandler.registerCommand("getconnectuuid", this);
    }

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
