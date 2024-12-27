package ru.ricardocraft.client.dto.request.websockets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.dto.NotificationEvent;
import ru.ricardocraft.client.dto.response.*;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.dto.request.WebSocketEvent;
import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.client.utils.ProviderMap;
import ru.ricardocraft.client.utils.UniversalJsonAdapter;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class ClientWebSocketService extends ClientJSONPoint {
    public static final ProviderMap<WebSocketEvent> results = new ProviderMap<>();
    public static final ProviderMap<WebSocketRequest> requests = new ProviderMap<>();
    private static boolean resultsRegistered = false;
    public final Gson gson;
    public final Boolean onConnect;
    public final Object waitObject = new Object();
    public OnCloseCallback onCloseCallback;
    public ReconnectCallback reconnectCallback;

    public ClientWebSocketService(String address) {
        super(createURL(address));
        this.gson = Launcher.gsonManager.gson;
        this.onConnect = true;
    }

    public static void appendTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(ClientProfile.Version.class, new ClientProfile.Version.GsonSerializer());
        builder.registerTypeAdapter(WebSocketEvent.class, new UniversalJsonAdapter<>(ClientWebSocketService.results));
        builder.registerTypeAdapter(WebSocketRequest.class, new UniversalJsonAdapter<>(ClientWebSocketService.requests));
        builder.registerTypeAdapter(AuthRequest.AuthPasswordInterface.class, new UniversalJsonAdapter<>(AuthRequest.providers));
        builder.registerTypeAdapter(GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails.class, new UniversalJsonAdapter<>(GetAvailabilityAuthRequest.providers));
        builder.registerTypeAdapter(OptionalAction.class, new UniversalJsonAdapter<>(OptionalAction.providers));
        builder.registerTypeAdapter(OptionalTrigger.class, new UniversalJsonAdapter<>(OptionalTrigger.providers));
    }

    private static URI createURL(String address) {
        try {
            return new URI(address);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    void onMessage(String message) {
        WebSocketEvent result = gson.fromJson(message, WebSocketEvent.class);
        eventHandle(result);
    }

    public abstract <T extends WebSocketEvent> void eventHandle(T event);

    @Override
    void onDisconnect(int statusCode, String reason) {
        LogHelper.info("WebSocket disconnected: %d: %s", statusCode, reason);
        if (onCloseCallback != null) onCloseCallback.onClose(statusCode, reason, !isClosed);
    }

    public void registerRequests() {

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

    public void waitIfNotConnected() {
    }

    public void sendObject(Object obj, Type type) throws IOException {
        waitIfNotConnected();
        if (webSocket == null || webSocket.isInputClosed()) reconnectCallback.onReconnect();
        send(gson.toJson(obj, type));
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
}