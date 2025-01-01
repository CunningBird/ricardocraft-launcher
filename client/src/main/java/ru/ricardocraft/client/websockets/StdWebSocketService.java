package ru.ricardocraft.client.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.ricardocraft.client.core.Downloader;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.client.dto.NotificationEvent;
import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.RequestException;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.client.dto.response.*;
import ru.ricardocraft.client.helper.JVMHelper;
import ru.ricardocraft.client.helper.LogHelper;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.utils.ProviderMap;
import ru.ricardocraft.client.utils.UniversalJsonAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class StdWebSocketService implements RequestService, WebSocket.Listener {
    private static boolean isCertificatePinning;
    private final URI uri;
    public boolean isClosed;
    private final WebSocket.Builder webSocketBuilder;
    protected HttpClient httpClient;
    protected WebSocket webSocket;
    protected boolean ssl = false;
    protected int port;
    private final Object syncObject = new Object();
    private volatile StringBuilder builder = new StringBuilder();
    public final Gson gson;
    public final Boolean onConnect;
    public OnCloseCallback onCloseCallback;
    public ReconnectCallback reconnectCallback;


    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<UUID, CompletableFuture> futureMap = new ConcurrentHashMap<>();
    private final HashSet<RequestService.EventHandler> eventHandlers = new HashSet<>();
    private final HashSet<StdWebSocketService.EventHandler> legacyEventHandlers = new HashSet<>();

    public StdWebSocketService(String address) throws Exception {
        URI uri = new URI(address);

        this.uri = uri;
        String protocol = uri.getScheme();
        if (!"ws".equals(protocol) && !"wss".equals(protocol)) {
            throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
        if ("wss".equals(protocol)) {
            ssl = true;
        }
        if (uri.getPort() == -1) {
            if ("ws".equals(protocol)) port = 80;
            else port = 443;
        } else port = uri.getPort();
        try {
            var httpClientBuilder = HttpClient.newBuilder();
            if(isCertificatePinning) {
                httpClientBuilder = httpClientBuilder.sslContext(Downloader.makeSSLContext());
            }
            httpClient = httpClientBuilder.build();
            webSocketBuilder = httpClient.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(30));
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException |
                 KeyManagementException e) {
            throw new RuntimeException(e);
        }

        this.gson = Launcher.gsonManager.gson;
        this.onConnect = true;
    }

    @Override
    public void registerEventHandler(RequestService.EventHandler handler) {
        eventHandlers.add(handler);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        synchronized (syncObject) {
            builder.append(data);
            if(last) {
                String message = builder.toString();
                builder = new StringBuilder();
                LogHelper.dev("Received %s", message);
                WebSocketEvent result = gson.fromJson(message, WebSocketEvent.class);
                eventHandle(result);
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        LogHelper.info("WebSocket disconnected: %d: %s", statusCode, reason);
        if (onCloseCallback != null) onCloseCallback.onClose(statusCode, reason, !isClosed);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LogHelper.error(error);
        WebSocket.Listener.super.onError(webSocket, error);
    }

    @SuppressWarnings({"unchecked"})
    public <T extends WebSocketEvent> void eventHandle(T webSocketEvent) {
        if (webSocketEvent instanceof RequestEvent event) {
            if (event.requestUUID == null) {
                LogHelper.warning("Request event type %s.requestUUID is null", event.getType() == null ? "null" : event.getType());
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

    public <T extends WebSocketEvent> void processEventHandlers(T event) {
        for (RequestService.EventHandler handler : eventHandlers) {
            if (handler.eventHandle(event)) return;
        }
        for (StdWebSocketService.EventHandler handler : legacyEventHandlers) {
            if (handler.eventHandle(event)) return;
        }
    }

    public <T extends WebSocketEvent> CompletableFuture<T> request(Request<T> request) throws IOException {
        CompletableFuture<T> result = new CompletableFuture<>();
        futureMap.put(request.requestUUID, result);

        if (webSocket == null || webSocket.isInputClosed()) reconnectCallback.onReconnect();
        String text = gson.toJson(request, WebSocketRequest.class);

        LogHelper.dev("Send %s", text);
        webSocket.sendText(text, true);

        return result;
    }

    public void open() throws Exception {
        webSocket = webSocketBuilder.buildAsync(uri, this).get();
    }

    public void openAsync(Runnable onConnect, Consumer<Throwable> onFail) {
        webSocketBuilder.buildAsync(uri, this).thenAccept((e) -> {
            this.webSocket = e;
            onConnect.run();
        }).exceptionally((ex) -> {
            onFail.accept(ex);
            return null;
        });
    }

    public void close() throws InterruptedException {
        webSocket.abort();
    }

    public interface ReconnectCallback {
        void onReconnect() throws IOException;
    }

    @FunctionalInterface
    public interface OnCloseCallback {
        void onClose(int code, String reason, boolean remote);
    }

    @FunctionalInterface
    public interface EventHandler {
        /**
         * @param event processing event
         * @param <T>   event type
         * @return false - continue, true - stop
         */
        <T extends WebSocketEvent> boolean eventHandle(T event);
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

}
