package ru.ricardocraft.backend.manangers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.core.MemoryAuthCoreProvider;
import ru.ricardocraft.backend.auth.core.MicrosoftAuthCoreProvider;
import ru.ricardocraft.backend.auth.core.MojangAuthCoreProvider;
import ru.ricardocraft.backend.auth.core.openid.OpenIDAuthCoreProvider;
import ru.ricardocraft.backend.auth.password.AcceptPasswordVerifier;
import ru.ricardocraft.backend.auth.password.PasswordVerifier;
import ru.ricardocraft.backend.auth.password.PlainPasswordVerifier;
import ru.ricardocraft.backend.auth.password.RejectPasswordVerifier;
import ru.ricardocraft.backend.auth.profiles.LocalProfileProvider;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.AdvancedProtectHandler;
import ru.ricardocraft.backend.auth.protect.NoProtectHandler;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.StdProtectHandler;
import ru.ricardocraft.backend.auth.texture.JsonTextureProvider;
import ru.ricardocraft.backend.auth.texture.RequestTextureProvider;
import ru.ricardocraft.backend.auth.texture.TextureProvider;
import ru.ricardocraft.backend.auth.texture.VoidTextureProvider;
import ru.ricardocraft.backend.auth.updates.LocalUpdatesProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.core.hasher.HashedEntry;
import ru.ricardocraft.backend.base.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.helper.CommonHelper;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.backend.base.profiles.optional.actions.OptionalActionFile;
import ru.ricardocraft.backend.base.profiles.optional.actions.OptionalActionJvmArgs;
import ru.ricardocraft.backend.base.profiles.optional.triggers.ArchTrigger;
import ru.ricardocraft.backend.base.profiles.optional.triggers.JavaTrigger;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OSTrigger;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.backend.base.request.JsonResultSerializeAdapter;
import ru.ricardocraft.backend.base.request.WebSocketEvent;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;
import ru.ricardocraft.backend.base.request.auth.details.AuthLoginOnlyDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthPasswordDetails;
import ru.ricardocraft.backend.base.request.auth.details.AuthWebViewDetails;
import ru.ricardocraft.backend.base.request.auth.password.*;
import ru.ricardocraft.backend.base.utils.ProviderMap;
import ru.ricardocraft.backend.base.utils.UniversalJsonAdapter;

@Component
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
    }

    public void registerAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(ClientProfile.Version.class, new ClientProfile.Version.GsonSerializer());
        builder.registerTypeAdapter(TextureProvider.class, new UniversalJsonAdapter<>(registerTextureProviders()));
        builder.registerTypeAdapter(AuthCoreProvider.class, new UniversalJsonAdapter<>(registerAuthCoreProviders()));
        builder.registerTypeAdapter(PasswordVerifier.class, new UniversalJsonAdapter<>(registerPasswordVerifierProviders()));
        builder.registerTypeAdapter(ProtectHandler.class, new UniversalJsonAdapter<>(registerHandlerProviders()));
        builder.registerTypeAdapter(WebSocketEvent.class, new JsonResultSerializeAdapter());
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
        passwordVerifierProviders.register("accept", AcceptPasswordVerifier.class);
        passwordVerifierProviders.register("reject", RejectPasswordVerifier.class);
        return passwordVerifierProviders;
    }

    public ProviderMap<TextureProvider> registerTextureProviders() {
        ProviderMap<TextureProvider> textureProviders = new ProviderMap<>("TextureProvider");
        textureProviders.register("void", VoidTextureProvider.class);
        textureProviders.register("request", RequestTextureProvider.class);
        textureProviders.register("json", JsonTextureProvider.class);
        return textureProviders;
    }

    public ProviderMap<ProtectHandler> registerHandlerProviders() {
        ProviderMap<ProtectHandler> protectHandlerProviders = new ProviderMap<>("ProtectHandler");
        protectHandlerProviders.register("none", NoProtectHandler.class);
        protectHandlerProviders.register("std", StdProtectHandler.class);
        protectHandlerProviders.register("advanced", AdvancedProtectHandler.class);
        return protectHandlerProviders;
    }

    public ProviderMap<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> registerGetAvailabilityAuthProviders() {
        ProviderMap<GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails> getAvailabilityAuthProviders = new ProviderMap<>();
        getAvailabilityAuthProviders.register("password", AuthPasswordDetails.class);
        getAvailabilityAuthProviders.register("webview", AuthWebViewDetails.class);
        getAvailabilityAuthProviders.register("loginonly", AuthLoginOnlyDetails.class);
        return getAvailabilityAuthProviders;
    }

    public ProviderMap<OptionalAction> registerOptionalActionProviders() {
        ProviderMap<OptionalAction> optionalActionProviders = new ProviderMap<>();
        optionalActionProviders.register("file", OptionalActionFile.class);
        optionalActionProviders.register("jvmArgs", OptionalActionJvmArgs.class);
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
