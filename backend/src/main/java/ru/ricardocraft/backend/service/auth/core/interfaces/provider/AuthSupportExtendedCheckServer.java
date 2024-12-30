package ru.ricardocraft.backend.service.auth.core.interfaces.provider;

import ru.ricardocraft.backend.service.auth.core.UserSession;
import ru.ricardocraft.backend.controller.Client;

import java.io.IOException;

public interface AuthSupportExtendedCheckServer {
    UserSession extendedCheckServer(Client client, String username, String serverID) throws IOException;
}
