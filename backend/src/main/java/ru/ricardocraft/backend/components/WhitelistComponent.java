package ru.ricardocraft.backend.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.ricardocraft.backend.Reconfigurable;
import ru.ricardocraft.backend.command.utls.Command;
import ru.ricardocraft.backend.command.utls.SubCommand;
import ru.ricardocraft.backend.manangers.AuthHookManager;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.socket.response.auth.JoinServerResponse;
import ru.ricardocraft.backend.utils.HookException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class WhitelistComponent extends Component implements AutoCloseable, Reconfigurable {
    private transient final Logger logger = LogManager.getLogger();
    public String message = "auth.message.techwork";
    public boolean enabled = true;
    public List<String> whitelist = new ArrayList<>();

    private final transient AuthHookManager authHookManager;

    @Autowired
    public WhitelistComponent(AuthHookManager authHookManager) {
        this.authHookManager = authHookManager;
        this.authHookManager.preHook.registerHook(this::hookAuth);
        this.authHookManager.joinServerHook.registerHook(this::hookJoin);

        whitelist.add("CunningBird"); // TODO delete this component

        setComponentName("whitelist");
    }

    public boolean hookAuth(AuthResponse.AuthContext context, Client client) throws HookException {
        if (enabled) {
            if (!whitelist.contains(context.login)) {
                throw new HookException(message);
            }
        }
        return false;
    }

    public boolean hookJoin(JoinServerResponse response, Client client) throws HookException {
        if (enabled) {
            if (!whitelist.contains(response.username)) {
                throw new HookException(message);
            }
        }
        return false;
    }

    @Override
    public void close() {
        this.authHookManager.preHook.unregisterHook(this::hookAuth);
        this.authHookManager.joinServerHook.unregisterHook(this::hookJoin);
    }

    @Override
    public Map<String, Command> getCommands() {
        var commands = defaultCommandsMap();
        commands.put("setmessage", new SubCommand("[new message]", "set message") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                message = args[0];
                logger.info("Message: {}", args[0]);
            }
        });
        commands.put("whitelist.add", new SubCommand("[login]", "add login to whitelist") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                whitelist.add(args[0]);
                logger.info("{} added to whitelist", args[0]);
            }
        });
        commands.put("whitelist.remove", new SubCommand("[login]", "remove login from whitelist") {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                whitelist.remove(args[0]);
                logger.info("{} removed from whitelist", args[0]);
            }
        });
        commands.put("disable", new SubCommand() {
            @Override
            public void invoke(String... args) {
                enabled = false;
                logger.info("Whitelist disabled");
            }
        });
        commands.put("enable", new SubCommand() {
            @Override
            public void invoke(String... args) {
                enabled = true;
                logger.info("Whitelist enabled");
            }
        });
        return commands;
    }
}
