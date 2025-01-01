package ru.ricardocraft.client.dto.request.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.ricardocraft.client.core.Downloader;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.core.LauncherInject;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.client.dto.NotificationEvent;
import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.RequestException;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.dto.request.WebSocketEvent;
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

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class StdWebSocketService implements RequestService, WebSocket.Listener {

    @LauncherInject("launcher.certificatePinning")
    private static boolean isCertificatePinning;
    private static final AtomicInteger counter = new AtomicInteger();
    private final URI uri;
    public boolean isClosed;
    private final WebSocket.Builder webSocketBuilder;
    protected HttpClient httpClient;
    protected WebSocket webSocket;
    protected boolean ssl = false;
    protected int port;
    private final Object syncObject = new Object();
    private final Object sendSyncObject = new Object();
    private volatile StringBuilder builder = new StringBuilder();

    public static final ProviderMap<WebSocketEvent> results = new ProviderMap<>();
    public static final ProviderMap<WebSocketRequest> requests = new ProviderMap<>();
    private static boolean resultsRegistered = false;
    public final Gson gson;
    public final Boolean onConnect;
    public final Object waitObject = new Object();
    public OnCloseCallback onCloseCallback;
    public ReconnectCallback reconnectCallback;


    @SuppressWarnings("rawtypes")
    private final ConcurrentHashMap<UUID, CompletableFuture> futureMap = new ConcurrentHashMap<>();
    private final HashSet<RequestService.EventHandler> eventHandlers = new HashSet<>();
    private final HashSet<StdWebSocketService.EventHandler> legacyEventHandlers = new HashSet<>();

    public StdWebSocketService(String address) throws SSLException {
        URI uri = createURL(address);

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

    public static void appendTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(ClientProfile.Version.class, new ClientProfile.Version.GsonSerializer());
        builder.registerTypeAdapter(WebSocketEvent.class, new UniversalJsonAdapter<>(StdWebSocketService.results));
        builder.registerTypeAdapter(WebSocketRequest.class, new UniversalJsonAdapter<>(StdWebSocketService.requests));
        builder.registerTypeAdapter(AuthRequest.AuthPasswordInterface.class, new UniversalJsonAdapter<>(AuthRequest.providers));
        builder.registerTypeAdapter(GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails.class, new UniversalJsonAdapter<>(GetAvailabilityAuthRequest.providers));
        builder.registerTypeAdapter(OptionalAction.class, new UniversalJsonAdapter<>(OptionalAction.providers));
        builder.registerTypeAdapter(OptionalTrigger.class, new UniversalJsonAdapter<>(OptionalTrigger.providers));
    }

    public void registerResults() {
        if (!resultsRegistered) {
            results.register("auth", AuthRequestEvent.class);
            results.register("checkServer", CheckServerRequestEvent.class);
            results.register("joinServer", JoinServerRequestEvent.class);
            results.register("launcher", LauncherRequestEvent.class);
            results.register("profileByUsername", ProfileByUsernameRequestEvent.class);
            results.register("profileByUUID", ProfileByUUIDRequestEvent.class);
            results.register("batchProfileByUsername", BatchProfileByUsernameRequestEvent.class);
            results.register("profiles", ProfilesRequestEvent.class);
            results.register("setProfile", SetProfileRequestEvent.class);
            results.register("error", ErrorRequestEvent.class);
            results.register("update", UpdateRequestEvent.class);
            results.register("getAvailabilityAuth", GetAvailabilityAuthRequestEvent.class);
            results.register("notification", NotificationEvent.class);
            results.register("exit", ExitRequestEvent.class);
            results.register("getSecureLevelInfo", GetSecureLevelInfoRequestEvent.class);
            results.register("verifySecureLevelKey", VerifySecureLevelKeyRequestEvent.class);
            results.register("securityReport", SecurityReportRequestEvent.class);
            results.register("hardwareReport", HardwareReportRequestEvent.class);
            results.register("currentUser", CurrentUserRequestEvent.class);
            results.register("features", FeaturesRequestEvent.class);
            results.register("refreshToken", RefreshTokenRequestEvent.class);
            results.register("restore", RestoreRequestEvent.class);
            results.register("additionalData", AdditionalDataRequestEvent.class);
            results.register("clientProfileKey", FetchClientProfileKeyRequestEvent.class);
            results.register("getPublicKey", GetPublicKeyRequestEvent.class);
            results.register("getAssetUploadUrl", GetAssetUploadUrlRequestEvent.class);
            results.register("assetUploadInfo", AssetUploadInfoRequestEvent.class);
            results.register("getConnectUUID", GetConnectUUIDRequestEvent.class);
            resultsRegistered = true;
        }
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
                    LogHelper.error(e);
                }
            }));
        }, future::completeExceptionally);
        return future;
    }


    @Deprecated
    public void registerEventHandler(StdWebSocketService.EventHandler handler) {
        legacyEventHandlers.add(handler);
    }

    @Deprecated
    public void unregisterEventHandler(StdWebSocketService.EventHandler handler) {
        legacyEventHandlers.remove(handler);
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

    private static URI createURL(String address) {
        try {
            return new URI(address);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends WebSocketEvent> void processEventHandlers(T event) {
        for (RequestService.EventHandler handler : eventHandlers) {
            if (handler.eventHandle(event)) return;
        }
        for (StdWebSocketService.EventHandler handler : legacyEventHandlers) {
            if (handler.eventHandle(event)) return;
        }
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
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        synchronized (syncObject) {
            builder.append(data);
            if(last) {
                String message = builder.toString();
                builder = new StringBuilder();
                LogHelper.dev("Received %s", message);
                onMessage(message);
            }
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    void onMessage(String message) {
        WebSocketEvent result = gson.fromJson(message, WebSocketEvent.class);
        eventHandle(result);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        onDisconnect(statusCode, reason);
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        LogHelper.error(error);
        WebSocket.Listener.super.onError(webSocket, error);
    }

    void onDisconnect(int statusCode, String reason) {
        LogHelper.info("WebSocket disconnected: %d: %s", statusCode, reason);
        if (onCloseCallback != null) onCloseCallback.onClose(statusCode, reason, !isClosed);
    }

    public void registerRequests() {

    }

    public void waitIfNotConnected() {
    }

    public void sendObject(Object obj, Type type) throws IOException {
        waitIfNotConnected();
        if (webSocket == null || webSocket.isInputClosed()) reconnectCallback.onReconnect();
        send(gson.toJson(obj, type));
    }

    public void send(String text) {
        LogHelper.dev("Send %s", text);
        webSocket.sendText(text, true);
    }

    public void close() throws InterruptedException {
        webSocket.abort();
    }

    @FunctionalInterface
    public interface OnCloseCallback {
        void onClose(int code, String reason, boolean remote);
    }

    public interface ReconnectCallback {
        void onReconnect() throws IOException;
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
