package ru.ricardocraft.backend.manangers;

import com.google.gson.GsonBuilder;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.mix.MixProvider;
import ru.ricardocraft.backend.auth.password.PasswordVerifier;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.texture.TextureProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.backend.base.request.JsonResultSerializeAdapter;
import ru.ricardocraft.backend.base.request.WebSocketEvent;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.base.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.backend.components.Component;
import ru.ricardocraft.backend.core.managers.GsonManager;
import ru.ricardocraft.backend.socket.WebSocketService;
import ru.ricardocraft.backend.socket.response.UnknownResponse;
import ru.ricardocraft.backend.socket.response.WebSocketServerResponse;
import ru.ricardocraft.backend.utils.UniversalJsonAdapter;

public class LaunchServerGsonManager extends GsonManager {

    @Override
    public void registerAdapters(GsonBuilder builder) {
        super.registerAdapters(builder);
        builder.registerTypeAdapter(ClientProfile.Version.class, new ClientProfile.Version.GsonSerializer());
        builder.registerTypeAdapter(TextureProvider.class, new UniversalJsonAdapter<>(TextureProvider.providers));
        builder.registerTypeAdapter(AuthCoreProvider.class, new UniversalJsonAdapter<>(AuthCoreProvider.providers));
        builder.registerTypeAdapter(PasswordVerifier.class, new UniversalJsonAdapter<>(PasswordVerifier.providers));
        builder.registerTypeAdapter(Component.class, new UniversalJsonAdapter<>(Component.providers));
        builder.registerTypeAdapter(ProtectHandler.class, new UniversalJsonAdapter<>(ProtectHandler.providers));
        builder.registerTypeAdapter(WebSocketServerResponse.class, new UniversalJsonAdapter<>(WebSocketService.providers, UnknownResponse.class));
        builder.registerTypeAdapter(WebSocketEvent.class, new JsonResultSerializeAdapter());
        builder.registerTypeAdapter(AuthRequest.AuthPasswordInterface.class, new UniversalJsonAdapter<>(AuthRequest.providers));
        builder.registerTypeAdapter(GetAvailabilityAuthRequestEvent.AuthAvailabilityDetails.class, new UniversalJsonAdapter<>(GetAvailabilityAuthRequest.providers));
        builder.registerTypeAdapter(OptionalAction.class, new UniversalJsonAdapter<>(OptionalAction.providers));
        builder.registerTypeAdapter(OptionalTrigger.class, new UniversalJsonAdapter<>(OptionalTrigger.providers));
        builder.registerTypeAdapter(MixProvider.class, new UniversalJsonAdapter<>(MixProvider.providers));
        builder.registerTypeAdapter(ProfileProvider.class, new UniversalJsonAdapter<>(ProfileProvider.providers));
        builder.registerTypeAdapter(UpdatesProvider.class, new UniversalJsonAdapter<>(UpdatesProvider.providers));
    }
}
