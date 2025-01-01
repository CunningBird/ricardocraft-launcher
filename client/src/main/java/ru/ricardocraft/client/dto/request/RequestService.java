package ru.ricardocraft.client.dto.request;

import ru.ricardocraft.client.dto.response.WebSocketEvent;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RequestService {
    <T extends WebSocketEvent> CompletableFuture<T> request(Request<T> request) throws IOException;
    void open() throws Exception;

    void registerEventHandler(EventHandler handler);

    default <T extends WebSocketEvent> T requestSync(Request<T> request) throws IOException {
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

    @FunctionalInterface
    interface EventHandler {
        /**
         * @param event processing event
         * @param <T>   event type
         * @return false - continue, true - stop
         */
        <T extends WebSocketEvent> boolean eventHandle(T event);
    }
}

