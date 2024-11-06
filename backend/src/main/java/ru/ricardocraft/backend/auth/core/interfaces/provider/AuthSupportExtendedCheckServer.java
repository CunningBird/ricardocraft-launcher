package ru.ricardocraft.backend.auth.core.interfaces.provider;

import ru.ricardocraft.backend.auth.core.UserSession;
import ru.ricardocraft.backend.socket.Client;

import java.io.IOException;

public interface AuthSupportExtendedCheckServer {
    UserSession extendedCheckServer(Client client, String username, String serverID) throws IOException;
}
