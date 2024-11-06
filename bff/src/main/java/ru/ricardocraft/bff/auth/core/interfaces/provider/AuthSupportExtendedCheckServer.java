package ru.ricardocraft.bff.auth.core.interfaces.provider;

import ru.ricardocraft.bff.auth.core.UserSession;
import ru.ricardocraft.bff.socket.Client;

import java.io.IOException;

public interface AuthSupportExtendedCheckServer {
    UserSession extendedCheckServer(Client client, String username, String serverID) throws IOException;
}
