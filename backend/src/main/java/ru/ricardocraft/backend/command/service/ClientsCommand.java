package ru.ricardocraft.backend.command.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.socket.ServerWebSocketHandler;

import java.io.IOException;
import java.util.Base64;

@Component
public class ClientsCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(ClientsCommand.class);

    private transient final ServerWebSocketHandler handler;

    public ClientsCommand(ServerWebSocketHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Show all connected clients";
    }

    @Override
    public void invoke(String... args) throws IOException {
        handler.forEachActiveChannels((session, client) -> {
            String ip = IOHelper.getIP(session.getRemoteAddress());
            if (!client.isAuth)
                logger.info("Channel {} | connectUUID {} | checkSign {}", ip, session.getId(), client.checkSign ? "true" : "false");
            else {
                logger.info("Client name {} | ip {} | connectUUID {}", client.username == null ? "null" : client.username, ip, session.getId());
                logger.info("userUUID: {}", client.uuid == null ? "null" : client.uuid.toString());
                logger.info("OAuth session {}", client.sessionObject == null ? "null" : client.sessionObject);
                logger.info("Data: checkSign {} | auth_id {}", client.checkSign ? "true" : "false", client.auth_id);
            }
            if (client.trustLevel != null) {
                logger.info("trustLevel | key {} | pubkey {}", client.trustLevel.keyChecked ? "checked" : "unchecked", client.trustLevel.publicKey == null ? "null" : new String(Base64.getEncoder().encode(client.trustLevel.publicKey)));
            }
            if (client.permissions != null) {
                logger.info("Permissions: {}", client.permissions.toString());
            }
        });
    }
}
