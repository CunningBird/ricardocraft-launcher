package ru.ricardocraft.bff.components;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.auth.AuthResponse;
import ru.ricardocraft.bff.utils.HookException;

public class AuthLimiterComponent extends IPLimiter implements AutoCloseable {
    public String message;
    private transient LaunchServer srv;

    @Override
    public void init(LaunchServer launchServer) {
        srv = launchServer;
        launchServer.authHookManager.preHook.registerHook(this::preAuthHook);
    }

    public boolean preAuthHook(AuthResponse.AuthContext context, Client client) {
        if (!check(context.ip)) {
            throw new HookException(message);
        }
        return false;
    }

    @Override
    public void close() {
        srv.authHookManager.preHook.unregisterHook(this::preAuthHook);
    }
}
