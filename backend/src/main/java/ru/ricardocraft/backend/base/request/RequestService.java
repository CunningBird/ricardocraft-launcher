package ru.ricardocraft.backend.base.request;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface RequestService {
    <T extends TypeSerializeInterface> CompletableFuture<T> request(Request<T> request) throws IOException;
    void open() throws Exception;

    void registerEventHandler(EventHandler handler);

    void unregisterEventHandler(EventHandler handler);

    default <T extends TypeSerializeInterface> T requestSync(Request<T> request) throws IOException {
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

    boolean isClosed();

    @FunctionalInterface
    interface EventHandler {
        /**
         * @param event processing event
         * @param <T>   event type
         * @return false - continue, true - stop
         */
        <T extends TypeSerializeInterface> boolean eventHandle(T event);
    }
}

