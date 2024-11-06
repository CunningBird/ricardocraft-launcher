package ru.ricardocraft.backend.base.request.websockets;

import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.RequestException;
import ru.ricardocraft.backend.base.request.RequestService;
import ru.ricardocraft.backend.base.request.WebSocketEvent;

import java.util.concurrent.CompletableFuture;

public class VoidRequestService implements RequestService {
    private final Throwable ex;

    public VoidRequestService(Throwable ex) {
        this.ex = ex;
    }

    public VoidRequestService() {
        this.ex = null;
    }

    @Override
    public <T extends WebSocketEvent> CompletableFuture<T> request(Request<T> request) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(ex != null ? ex : new RequestException("Connection fail"));
        return future;
    }

    @Override
    public void open() {

    }

    @Override
    public void registerEventHandler(EventHandler handler) {

    }

    @Override
    public void unregisterEventHandler(EventHandler handler) {

    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
