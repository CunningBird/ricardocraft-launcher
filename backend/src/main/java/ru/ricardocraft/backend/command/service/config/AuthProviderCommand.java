package ru.ricardocraft.backend.command.service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.service.config.authProvider.AuthProviderAuthCommand;
import ru.ricardocraft.backend.command.service.config.authProvider.AuthProviderGetUserByUsernameCommand;
import ru.ricardocraft.backend.command.service.config.authProvider.AuthProviderGetUserByUuidCommand;

@Component
public class AuthProviderCommand extends Command {

    @Autowired
    public AuthProviderCommand(AuthProviderAuthCommand authProviderAuthCommand,
                               AuthProviderGetUserByUsernameCommand authProviderGetUserByUsernameCommand,
                               AuthProviderGetUserByUuidCommand authProviderGetUserByUuidCommand
    ) {
        super();
        this.childCommands.put("auth", authProviderAuthCommand);
        this.childCommands.put("getuserbyusername", authProviderGetUserByUsernameCommand);
        this.childCommands.put("getuserbyuuid", authProviderGetUserByUuidCommand);


    }

    @Override
    public String getArgsDescription() {
        return "[subcommand] [args...]";
    }

    @Override
    public String getUsageDescription() {
        return "manage proGuard";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
