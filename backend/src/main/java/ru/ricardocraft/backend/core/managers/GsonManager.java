package ru.ricardocraft.backend.core.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.MemoryAuthCoreProvider;
import ru.ricardocraft.backend.auth.core.MicrosoftAuthCoreProvider;
import ru.ricardocraft.backend.auth.core.MojangAuthCoreProvider;
import ru.ricardocraft.backend.auth.core.openid.OpenIDAuthCoreProvider;
import ru.ricardocraft.backend.auth.password.*;
import ru.ricardocraft.backend.auth.profiles.LocalProfileProvider;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.auth.protect.NoProtectHandler;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.StdProtectHandler;
import ru.ricardocraft.backend.auth.texture.*;
import ru.ricardocraft.backend.auth.updates.LocalUpdatesProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.profiles.optional.actions.*;
import ru.ricardocraft.backend.base.profiles.optional.triggers.ArchTrigger;
import ru.ricardocraft.backend.base.profiles.optional.triggers.JavaTrigger;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OSTrigger;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.backend.base.request.JsonResultSerializeAdapter;
import ru.ricardocraft.backend.base.request.WebSocketEvent;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.base.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthTotpDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.backend.base.request.auth.password.*;
import ru.ricardocraft.backend.components.AuthLimiterComponent;
import ru.ricardocraft.backend.components.Component;
import ru.ricardocraft.backend.components.ProGuardComponent;
import ru.ricardocraft.backend.core.hasher.HashedEntry;
import ru.ricardocraft.backend.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.backend.helper.CommonHelper;
import ru.ricardocraft.backend.socket.response.UnknownResponse;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.socket.response.auth.*;
import ru.ricardocraft.backend.socket.response.cabinet.AssetUploadInfoResponse;
import ru.ricardocraft.backend.socket.response.cabinet.GetAssetUploadInfoResponse;
import ru.ricardocraft.backend.socket.response.management.FeaturesResponse;
import ru.ricardocraft.backend.socket.response.management.GetConnectUUIDResponse;
import ru.ricardocraft.backend.socket.response.management.GetPublicKeyResponse;
import ru.ricardocraft.backend.socket.response.profile.BatchProfileByUsername;
import ru.ricardocraft.backend.socket.response.profile.ProfileByUUIDResponse;
import ru.ricardocraft.backend.socket.response.profile.ProfileByUsername;
import ru.ricardocraft.backend.socket.response.secure.GetSecureLevelInfoResponse;
import ru.ricardocraft.backend.socket.response.secure.HardwareReportResponse;
import ru.ricardocraft.backend.socket.response.secure.SecurityReportResponse;
import ru.ricardocraft.backend.socket.response.secure.VerifySecureLevelKeyResponse;
import ru.ricardocraft.backend.socket.response.update.LauncherResponse;
import ru.ricardocraft.backend.socket.response.update.UpdateResponse;
import ru.ricardocraft.backend.utils.ProviderMap;
import ru.ricardocraft.backend.utils.UniversalJsonAdapter;

@org.springframework.stereotype.Component
public class GsonManager {

    public GsonBuilder configGsonBuilder;
    public GsonBuilder gsonBuilder;

    public Gson gson;
    public Gson configGson;

    public GsonManager() {
        gsonBuilder = CommonHelper.newBuilder();
        configGsonBuilder = CommonHelper.newBuilder();
        configGsonBuilder.setPrettyPrinting().disableHtmlEscaping();
        registerAdapters(gsonBuilder);
        registerAdapters(configGsonBuilder);
        gson = gsonBuilder.create();
        configGson = configGsonBuilder.create();

        Launcher.gsonManager = this;
    }

    public void registerAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(ClientProfile.Version.class, new ClientProfile.Version.GsonSerializer());
        builder.registerTypeAdapter(TextureProvider.class, new UniversalJsonAdapter<>(registerTextureProviders()));
        builder.registerTypeAdapter(AuthCoreProvider.class, new UniversalJsonAdapter<>(registerAuthCoreProviders()));
        builder.registerTypeAdapter(PasswordVerifier.class, new UniversalJsonAdapter<>(registerPasswordVerifierProviders()));
        builder.registerTypeAdapter(Component.class, new UniversalJsonAdapter<>(registerComponentsProviders()));
        builder.registerTypeAdapter(ProtectHandler.class, new UniversalJsonAdapter<>(registerHandlerProviders()));
        builder.registerTypeAdapter(WebSocketServerResponse.class, new UniversalJsonAdapter<>(registerWebSocketResponseProviders(), UnknownResponse.class));
        builder.registerTypeAdapter(WebSocketEvent.class, new JsonResultSerializeAdapter());
        builder.registerTypeAdapter(AuthRequest.AuthPasswordInterface.class, new UniversalJsonAdapter<>(registerAuthRequestProviders()));
        builder.registerTypeAdapter(GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails.class, new UniversalJsonAdapter<>(registerGetAvailabilityAuthProviders()));
        builder.registerTypeAdapter(OptionalAction.class, new UniversalJsonAdapter<>(registerOptionalActionProviders()));
        builder.registerTypeAdapter(OptionalTrigger.class, new UniversalJsonAdapter<>(registerOptionalTriggerProviders()));
        builder.registerTypeAdapter(ProfileProvider.class, new UniversalJsonAdapter<>(registerProfileProviders()));
        builder.registerTypeAdapter(UpdatesProvider.class, new UniversalJsonAdapter<>(registerUpdatesProviders()));
    }

    public ProviderMap<AuthCoreProvider> registerAuthCoreProviders() {
        ProviderMap<AuthCoreProvider> authCoreProviders = new ProviderMap<>("AuthCoreProvider");
        authCoreProviders.register("memory", MemoryAuthCoreProvider.class);
        authCoreProviders.register("openid", OpenIDAuthCoreProvider.class);
        authCoreProviders.register("mojang", MojangAuthCoreProvider.class);
        authCoreProviders.register("microsoft", MicrosoftAuthCoreProvider.class);
        return authCoreProviders;
    }

    public ProviderMap<PasswordVerifier> registerPasswordVerifierProviders() {
        ProviderMap<PasswordVerifier> passwordVerifierProviders = new ProviderMap<>("PasswordVerifier");
        passwordVerifierProviders.register("plain", PlainPasswordVerifier.class);
        passwordVerifierProviders.register("digest", DigestPasswordVerifier.class);
        passwordVerifierProviders.register("doubleDigest", DoubleDigestPasswordVerifier.class);
        passwordVerifierProviders.register("json", JsonPasswordVerifier.class);
        passwordVerifierProviders.register("bcrypt", BCryptPasswordVerifier.class);
        passwordVerifierProviders.register("accept", AcceptPasswordVerifier.class);
        passwordVerifierProviders.register("reject", RejectPasswordVerifier.class);
        return passwordVerifierProviders;
    }

    public ProviderMap<TextureProvider> registerTextureProviders() {
        ProviderMap<TextureProvider> textureProviders = new ProviderMap<>("TextureProvider");
        textureProviders.register("null", NullTextureProvider.class);
        textureProviders.register("void", VoidTextureProvider.class);

        // Auth providers that doesn't do nothing :D
        textureProviders.register("request", RequestTextureProvider.class);
        textureProviders.register("json", JsonTextureProvider.class);
        return textureProviders;
    }

    public ProviderMap<Component> registerComponentsProviders() {
        ProviderMap<Component> componentProviders = new ProviderMap<>();
        componentProviders.register("authLimiter", AuthLimiterComponent.class);
        componentProviders.register("proguard", ProGuardComponent.class);
        return componentProviders;
    }

    public ProviderMap<ProtectHandler> registerHandlerProviders() {
        ProviderMap<ProtectHandler> protectHandlerProviders = new ProviderMap<>("ProtectHandler");
        protectHandlerProviders.register("none", NoProtectHandler.class);
        protectHandlerProviders.register("std", StdProtectHandler.class);
        protectHandlerProviders.register("advanced", AdvancedProtectHandler.class);
        return protectHandlerProviders;
    }

    public ProviderMap<WebSocketServerResponse> registerWebSocketResponseProviders() {
        ProviderMap<WebSocketServerResponse> webSocketResponseProviders = new ProviderMap<>();

        // Auth
        webSocketResponseProviders.register("additionalData", AdditionalDataResponse.class);
        webSocketResponseProviders.register("auth", AuthResponse.class);
        webSocketResponseProviders.register("checkServer", CheckServerResponse.class);
        webSocketResponseProviders.register("currentUser", CurrentUserResponse.class);
        webSocketResponseProviders.register("exit", ExitResponse.class);
        webSocketResponseProviders.register("clientProfileKey", FetchClientProfileKeyResponse.class);
        webSocketResponseProviders.register("getAvailabilityAuth", GetAvailabilityAuthResponse.class);
        webSocketResponseProviders.register("joinServer", JoinServerResponse.class);
        webSocketResponseProviders.register("profiles", ProfilesResponse.class);
        webSocketResponseProviders.register("refreshToken", RefreshTokenResponse.class);
        webSocketResponseProviders.register("restore", RestoreResponse.class);
        webSocketResponseProviders.register("setProfile", SetProfileResponse.class);

        // Update
        webSocketResponseProviders.register("launcher", LauncherResponse.class);
        webSocketResponseProviders.register("update", UpdateResponse.class);

        // Profile
        webSocketResponseProviders.register("batchProfileByUsername", BatchProfileByUsername.class);
        webSocketResponseProviders.register("profileByUsername", ProfileByUsername.class);
        webSocketResponseProviders.register("profileByUUID", ProfileByUUIDResponse.class);

        // Secure
        webSocketResponseProviders.register("getSecureLevelInfo", GetSecureLevelInfoResponse.class);
        webSocketResponseProviders.register("hardwareReport", HardwareReportResponse.class);
        webSocketResponseProviders.register("securityReport", SecurityReportResponse.class);
        webSocketResponseProviders.register("verifySecureLevelKey", VerifySecureLevelKeyResponse.class);

        // Management
        webSocketResponseProviders.register("features", FeaturesResponse.class);
        webSocketResponseProviders.register("getConnectUUID", GetConnectUUIDResponse.class);
        webSocketResponseProviders.register("getPublicKey", GetPublicKeyResponse.class);

        // Cabinet
        webSocketResponseProviders.register("assetUploadInfo", AssetUploadInfoResponse.class);
        webSocketResponseProviders.register("getAssetUploadUrl", GetAssetUploadInfoResponse.class);

        return webSocketResponseProviders;
    }

    public ProviderMap<AuthRequest.AuthPasswordInterface> registerAuthRequestProviders() {
        ProviderMap<AuthRequest.AuthPasswordInterface> authRequestProviders = new ProviderMap<>();
        authRequestProviders.register("plain", AuthPlainPassword.class);
        authRequestProviders.register("rsa2", AuthRSAPassword.class);
        authRequestProviders.register("aes", AuthAESPassword.class);
        authRequestProviders.register("2fa", Auth2FAPassword.class);
        authRequestProviders.register("multi", AuthMultiPassword.class);
        authRequestProviders.register("signature", AuthSignaturePassword.class);
        authRequestProviders.register("totp", AuthTOTPPassword.class);
        authRequestProviders.register("oauth", AuthOAuthPassword.class);
        authRequestProviders.register("code", AuthCodePassword.class);
        return authRequestProviders;
    }

    public ProviderMap<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> registerGetAvailabilityAuthProviders() {
        ProviderMap<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getAvailabilityAuthProviders = new ProviderMap<>();
        getAvailabilityAuthProviders.register("password", AuthPasswordDetails.class);
        getAvailabilityAuthProviders.register("webview", AuthWebViewDetails.class);
        getAvailabilityAuthProviders.register("totp", AuthTotpDetails.class);
        getAvailabilityAuthProviders.register("loginonly", AuthLoginOnlyDetails.class);
        return getAvailabilityAuthProviders;
    }

    public ProviderMap<OptionalAction> registerOptionalActionProviders() {
        ProviderMap<OptionalAction> optionalActionProviders = new ProviderMap<>();
        optionalActionProviders.register("file", OptionalActionFile.class);
        optionalActionProviders.register("clientArgs", OptionalActionClientArgs.class);
        optionalActionProviders.register("jvmArgs", OptionalActionJvmArgs.class);
        optionalActionProviders.register("classpath", OptionalActionClassPath.class);
        return optionalActionProviders;
    }

    public ProviderMap<OptionalTrigger> registerOptionalTriggerProviders() {
        ProviderMap<OptionalTrigger> optionalTriggerProviders = new ProviderMap<>("OptionalTriggers");
        optionalTriggerProviders.register("java", JavaTrigger.class);
        optionalTriggerProviders.register("os", OSTrigger.class);
        optionalTriggerProviders.register("arch", ArchTrigger.class);
        return optionalTriggerProviders;
    }

    public ProviderMap<ProfileProvider> registerProfileProviders() {
        ProviderMap<ProfileProvider> profileProviders = new ProviderMap<>("ProfileProvider");
        profileProviders.register("local", LocalProfileProvider.class);
        return profileProviders;
    }

    public ProviderMap<UpdatesProvider> registerUpdatesProviders() {
        ProviderMap<UpdatesProvider> updatesProviders = new ProviderMap<>("UpdatesProvider");
        updatesProviders.register("local", LocalUpdatesProvider.class);
        return updatesProviders;
    }
}
