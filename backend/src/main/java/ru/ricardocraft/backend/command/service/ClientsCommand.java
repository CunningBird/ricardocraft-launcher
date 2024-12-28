package ru.ricardocraft.backend.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.ricardocraft.backend.ServerWebSocketHandler;
import ru.ricardocraft.backend.base.helper.IOHelper;

import java.util.Base64;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class ClientsCommand {

    private final ServerWebSocketHandler handler;

    @ShellMethod("Show all connected clients")
    public void clients() {
        handler.forEachActiveChannels((session, client) -> {
            String ip = IOHelper.getIP(session.getRemoteAddress());
            if (!client.isAuth)
                log.info("Channel {} | connectUUID {} | checkSign {}", ip, session.getId(), client.checkSign ? "true" : "false");
            else {
                log.info("Client name {} | ip {} | connectUUID {}", client.username == null ? "null" : client.username, ip, session.getId());
                log.info("userUUID: {}", client.uuid == null ? "null" : client.uuid.toString());
                log.info("OAuth session {}", client.sessionObject == null ? "null" : client.sessionObject);
                log.info("Data: checkSign {} | auth_id {}", client.checkSign ? "true" : "false", client.auth_id);
            }
            if (client.trustLevel != null) {
                log.info("trustLevel | key {} | pubkey {}", client.trustLevel.keyChecked ? "checked" : "unchecked", client.trustLevel.publicKey == null ? "null" : new String(Base64.getEncoder().encode(client.trustLevel.publicKey)));
            }
            if (client.permissions != null) {
                log.info("Permissions: {}", client.permissions);
            }
        });
    }
}
