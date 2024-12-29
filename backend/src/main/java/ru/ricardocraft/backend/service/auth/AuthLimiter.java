package ru.ricardocraft.backend.service.auth;

import lombok.Getter;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.controller.auth.AuthController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class AuthLimiter {

    public String message = "Превышен лимит авторизаций";
    public final List<String> exclude = new ArrayList<>();
    @Getter
    private final transient Map<String, LimitEntry> map = new HashMap<>();
    public int rateLimit = 3;
    public long rateLimitMillis = SECONDS.toMillis(8);

    public void preAuthHook(AuthController.AuthContext context) throws Exception {
        if (!check(context.ip)) {
            throw new Exception(message);
        }
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
