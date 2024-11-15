package ru.ricardocraft.backend.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.manangers.AuthHookManager;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;
import ru.ricardocraft.backend.utils.HookException;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class AuthLimiterComponent extends IPLimiter implements AutoCloseable {
    public String message;
    private final transient AuthHookManager authHookManager;

    @Autowired
    public AuthLimiterComponent(AuthHookManager authHookManager) {
        this.authHookManager = authHookManager;

        this.rateLimit = 3;
        this.rateLimitMillis = SECONDS.toMillis(8);
        this.message = "Превышен лимит авторизаций";

        authHookManager.preHook.registerHook(this::preAuthHook);

        setComponentName("authLimiter");
    }

    public boolean preAuthHook(AuthResponseService.AuthContext context, Client client) {
        if (!check(context.ip)) {
            throw new HookException(message);
        }
        return false;
    }

    @Override
    public void close() {
        authHookManager.preHook.unregisterHook(this::preAuthHook);
    }
}
