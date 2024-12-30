package ru.ricardocraft.client.dto.request.websockets;

import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.RequestException;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.dto.request.WebSocketEvent;
import ru.ricardocraft.client.helper.LogHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class OfflineRequestService implements RequestService {
    private final HashSet<EventHandler> eventHandlers = new HashSet<>();
    private final Map<Class<?>, RequestProcessor<?, ?>> processors = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends WebSocketEvent> CompletableFuture<T> request(Request<T> request) {
        RequestProcessor<T, Request<T>> processor = (RequestProcessor<T, Request<T>>) processors.get(request.getClass());
        CompletableFuture<T> future = new CompletableFuture<>();
        if (processor == null) {
            future.completeExceptionally(new RequestException(String.format("Offline mode not support '%s'", request.getType())));
            return future;
        }
        if (LogHelper.isDevEnabled()) {
            LogHelper.dev("Request %s: %s", request.getType(), Launcher.gsonManager.gson.toJson(request));
        }
        try {
            T event = processor.process(request);
            if (LogHelper.isDevEnabled()) {
                LogHelper.dev("Response %s: %s", event.getType(), Launcher.gsonManager.gson.toJson(event));
            }
            future.complete(event);
        } catch (Throwable e) {
            if (e instanceof RequestException) {
                future.completeExceptionally(e);
            } else {
                future.completeExceptionally(new RequestException(e));
            }
        }
        return future;
    }

    @Override
    public void open() {

    }

    @Override
    public void registerEventHandler(EventHandler handler) {
        eventHandlers.add(handler);
    }

    public <T extends WebSocketEvent, V extends WebSocketRequest> void registerRequestProcessor(Class<V> requestClazz, RequestProcessor<T, V> function) {
        processors.put(requestClazz, function);
    }

    public interface RequestProcessor<T extends WebSocketEvent, V extends WebSocketRequest> {
        T process(V request) throws RequestException;
    }
}
