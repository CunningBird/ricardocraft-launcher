package ru.ricardocraft.client.core.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.client.dto.NotificationEvent;
import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.client.dto.response.*;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.client.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.client.websockets.StdWebSocketService;
import ru.ricardocraft.client.helper.CommonHelper;
import ru.ricardocraft.client.launch.RuntimeModuleManager;
import ru.ricardocraft.client.modules.events.PreGsonPhase;
import ru.ricardocraft.client.runtime.client.UserSettings;
import ru.ricardocraft.client.utils.ProviderMap;
import ru.ricardocraft.client.utils.UniversalJsonAdapter;
import ru.ricardocraft.client.websockets.WebSocketRequest;

@Component
public class GsonManager {

    public final static String RUNTIME_NAME = "stdruntime";
    private final ProviderMap<UserSettings> providers = new ProviderMap<>();
    private static boolean resultsRegistered = false;
    public static final ProviderMap<WebSocketEvent> results = new ProviderMap<>();
    public static final ProviderMap<WebSocketRequest> requests = new ProviderMap<>();

    public Gson gson;
    public Gson configGson;

    private final RuntimeModuleManager moduleManager;

    @Autowired
    public GsonManager(RuntimeModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        registerResults();

        providers.register(RUNTIME_NAME, RuntimeSettings.class);

        GsonBuilder gsonBuilder = CommonHelper.newBuilder();
        GsonBuilder configGsonBuilder = CommonHelper.newBuilder();
        configGsonBuilder.setPrettyPrinting().disableHtmlEscaping();
        registerAdapters(gsonBuilder);
        registerAdapters(configGsonBuilder);
        gson = gsonBuilder.create();
        configGson = configGsonBuilder.create();

        Launcher.gsonManager = this;
    }

    public void registerAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(UserSettings.class, new UniversalJsonAdapter<>(providers));
        appendTypeAdapters(builder);
        moduleManager.invokeEvent(new PreGsonPhase(builder));
    }

    public static void appendTypeAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(ClientProfile.Version.class, new ClientProfile.Version.GsonSerializer());
        builder.registerTypeAdapter(WebSocketEvent.class, new UniversalJsonAdapter<>(results));
        builder.registerTypeAdapter(WebSocketRequest.class, new UniversalJsonAdapter<>(requests));
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

}
