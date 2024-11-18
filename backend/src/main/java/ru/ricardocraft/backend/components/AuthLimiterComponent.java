package ru.ricardocraft.backend.components;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.auth.AuthResponseService;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.utils.HookException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class AuthLimiterComponent extends ru.ricardocraft.backend.components.Component {
    public String message;
    public final List<String> exclude = new ArrayList<>();
    @Getter
    private final transient Map<String, LimitEntry> map = new HashMap<>();
    public int rateLimit;
    public long rateLimitMillis;

    public AuthLimiterComponent() {
        this.rateLimit = 3;
        this.rateLimitMillis = SECONDS.toMillis(8);
        this.message = "Превышен лимит авторизаций";

        setComponentName("authLimiter");
    }

    public boolean preAuthHook(AuthResponseService.AuthContext context, Client client) {
        if (!check(context.ip)) {
            throw new HookException(message);
        }
        return false;
    }

    public String getFromString(String str) {
        return str;
    }

    public void garbageCollection() {
        long time = System.currentTimeMillis();
        map.entrySet().removeIf((e) -> e.getValue().time + rateLimitMillis < time);
    }

    public boolean check(String address) {
        if (exclude.contains(address)) return true;
        LimitEntry entry = map.get(address);
        if (entry == null) {
            map.put(address, new LimitEntry());
            return true;
        } else {
            long time = System.currentTimeMillis();
            if (entry.trys < rateLimit) {
                entry.trys++;
                entry.time = time;
                return true;
            }
            if (entry.time + rateLimitMillis < time) {
                entry.trys = 1;
                entry.time = time;
                return true;
            }
            return false;
        }
    }

    protected static class LimitEntry {
        long time;
        int trys;

        public LimitEntry() {
            time = System.currentTimeMillis();
            trys = 0;
        }
    }

}
