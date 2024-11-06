package ru.ricardocraft.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.mix.MixProvider;
import ru.ricardocraft.backend.auth.password.PasswordVerifier;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.texture.TextureProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.profiles.optional.actions.OptionalAction;
import ru.ricardocraft.backend.base.profiles.optional.triggers.OptionalTrigger;
import ru.ricardocraft.backend.base.request.auth.AuthRequest;
import ru.ricardocraft.backend.base.request.auth.GetAvailabilityAuthRequest;
import ru.ricardocraft.backend.components.Component;
import ru.ricardocraft.backend.core.managers.GsonManager;
import ru.ricardocraft.backend.manangers.LaunchServerGsonManager;
import ru.ricardocraft.backend.socket.WebSocketService;

@Configuration
public class GsonConfiguration {

    @Bean
    public GsonManager gsonManager() {
        AuthCoreProvider.registerProviders();
        PasswordVerifier.registerProviders();
        TextureProvider.registerProviders();
        Component.registerComponents();
        ProtectHandler.registerHandlers();
        WebSocketService.registerResponses();
        AuthRequest.registerProviders();
        GetAvailabilityAuthRequest.registerProviders();
        OptionalAction.registerProviders();
        OptionalTrigger.registerProviders();
        MixProvider.registerProviders();
        ProfileProvider.registerProviders();
        UpdatesProvider.registerProviders();

        GsonManager manager = new LaunchServerGsonManager();
        manager.initGson();

        Launcher.gsonManager = manager;
        return manager;
    }
}
