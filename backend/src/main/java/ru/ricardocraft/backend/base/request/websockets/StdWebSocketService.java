package ru.ricardocraft.backend.base.request.websockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ErrorRequestEvent;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.base.request.Request;
import ru.ricardocraft.backend.base.request.RequestException;
import ru.ricardocraft.backend.base.request.RequestService;
import ru.ricardocraft.backend.base.request.WebSocketEvent;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class StdWebSocketService extends ClientWebSocketService implements RequestService {

    private static final Logger logger = LoggerFactory.getLogger(StdWebSocketService.class);

    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<UUID, CompletableFuture> futureMap = new ConcurrentHashMap<>();
    private final HashSet<RequestService.EventHandler> eventHandlers = new HashSet<>();
    private final HashSet<ClientWebSocketService.EventHandler> legacyEventHandlers = new HashSet<>();

    public StdWebSocketService(String address) throws SSLException {
        super(address);
    }

    public static CompletableFuture<StdWebSocketService> initWebSockets(String address) {
        StdWebSocketService service;
        try {
            service = new StdWebSocketService(address);
        } catch (SSLException e) {
            throw new SecurityException(e);
        }
        service.registerResults();
        service.registerRequests();
        CompletableFuture<StdWebSocketService> future = new CompletableFuture<>();
        service.openAsync(() -> {
            future.complete(service);
            JVMHelper.RUNTIME.addShutdownHook(new Thread(() -> {
                try {
                    service.close();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
            }));
        }, future::completeExceptionally);
        return future;
    }


    @Deprecated
    public void registerEventHandler(ClientWebSocketService.EventHandler handler) {
        legacyEventHandlers.add(handler);
    }

    @Deprecated
    public void unregisterEventHandler(ClientWebSocketService.EventHandler handler) {
        legacyEventHandlers.remove(handler);
    }

    public <T extends WebSocketEvent> void processEventHandlers(T event) {
        for (RequestService.EventHandler handler : eventHandlers) {
            if (handler.eventHandle(event)) return;
        }
        for (ClientWebSocketService.EventHandler handler : legacyEventHandlers) {
            if (handler.eventHandle(event)) return;
        }
    }

    @SuppressWarnings({"unchecked"})
    public <T extends WebSocketEvent> void eventHandle(T webSocketEvent) {
        if (webSocketEvent instanceof RequestEvent event) {
            if (event.requestUUID == null) {
                logger.warn("Request event type {}.requestUUID is null", event.getType() == null ? "null" : event.getType());
                return;
            }
            if (event.requestUUID.equals(RequestEvent.eventUUID)) {
                processEventHandlers(webSocketEvent);
                return;
            }
            @SuppressWarnings("rawtypes")
            CompletableFuture future = futureMap.get(event.requestUUID);
            if (future != null) {
                if (event instanceof ErrorRequestEvent) {
                    future.completeExceptionally(new RequestException(((ErrorRequestEvent) event).error));
                } else
                    future.complete(event);
                futureMap.remove(event.requestUUID);
            } else {
                processEventHandlers(event);
                return;
            }
        }
        //
        processEventHandlers(webSocketEvent);
    }

    public <T extends WebSocketEvent> CompletableFuture<T> request(Request<T> request) throws IOException {
        CompletableFuture<T> result = new CompletableFuture<>();
        futureMap.put(request.requestUUID, result);
        sendObject(request, WebSocketRequest.class);
        return result;
    }

    @Override
    public void registerEventHandler(RequestService.EventHandler handler) {
        eventHandlers.add(handler);
    }

    @Override
    public void unregisterEventHandler(RequestService.EventHandler handler) {
        eventHandlers.remove(handler);
    }

    public <T extends WebSocketEvent> T requestSync(Request<T> request) throws IOException {
        try {
            return request(request).get();
        } catch (InterruptedException e) {
            throw new RequestException("Request interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException)
                throw (IOException) e.getCause();
            else {
                throw new RequestException(cause);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }
}
