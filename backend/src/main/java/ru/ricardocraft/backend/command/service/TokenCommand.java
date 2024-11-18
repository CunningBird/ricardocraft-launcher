package ru.ricardocraft.backend.command.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.mirror.token.TokenInfoCommand;
import ru.ricardocraft.backend.command.mirror.token.TokenServerCommand;

@Component
public class TokenCommand extends Command {

    @Autowired
    public TokenCommand(TokenInfoCommand tokenInfoCommand,
                        TokenServerCommand tokenServerCommand) {
        super();
        this.childCommands.put("info", tokenInfoCommand);
        this.childCommands.put("server", tokenServerCommand);
    }

    @Override
    public String getArgsDescription() {
        return "[server/info/token name] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "jwt management";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
