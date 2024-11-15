package ru.ricardocraft.backend.auth.protect;

import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.utils.ProviderMap;

public abstract class ProtectHandler {
    public static final ProviderMap<ProtectHandler> providers = new ProviderMap<>("ProtectHandler");
    private static boolean registredHandl = false;

    public static void registerHandlers() {
        if (!registredHandl) {
            providers.register("none", NoProtectHandler.class);
            providers.register("std", StdProtectHandler.class);
            providers.register("advanced", AdvancedProtectHandler.class);
            registredHandl = true;
        }
    }

    public abstract boolean allowGetAccessToken(AuthResponse.AuthContext context);
    public boolean allowJoinServer(Client client) {
        return client.isAuth && client.type == AuthResponse.ConnectTypes.CLIENT;
    }
}
