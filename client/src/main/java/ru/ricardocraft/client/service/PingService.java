package ru.ricardocraft.client.service;

import org.springframework.stereotype.Component;
import ru.ricardocraft.client.runtime.client.ServerPinger;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PingService {
    private final Map<String, CompletableFuture<PingServerReport>> reports = new ConcurrentHashMap<>();

    public CompletableFuture<PingServerReport> getPingReport(String serverName) {
        return reports.computeIfAbsent(serverName, k -> new CompletableFuture<>());
    }

    public void addReport(String name, ServerPinger.Result result) {
        CompletableFuture<PingServerReport> report = getPingReport(name);
        PingServerReport value = new PingServerReport(name, result.maxPlayers, result.onlinePlayers);
        report.complete(value);
    }

    public void clear() {
        reports.forEach((k, v) -> {
            if (!v.isDone()) {
                v.completeExceptionally(new InterruptedException());
            }
        });
        reports.clear();
    }

    public static class PingServerReport {
        public final String name;
        public final int maxPlayers;
        public final int playersOnline;

        public PingServerReport(String name, int maxPlayers, int playersOnline) {
            this.name = name;
            this.maxPlayers = maxPlayers;
            this.playersOnline = playersOnline;
        }
    }
}
