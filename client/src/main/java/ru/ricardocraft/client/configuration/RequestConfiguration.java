package ru.ricardocraft.client.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ricardocraft.client.service.ClientPermissions;
import ru.ricardocraft.client.dto.response.AuthRequestEvent;
import ru.ricardocraft.client.dto.response.ProfilesRequestEvent;
import ru.ricardocraft.client.base.helper.JVMHelper;
import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.service.profiles.PlayerProfile;
import ru.ricardocraft.client.dto.request.Request;
import ru.ricardocraft.client.dto.request.RequestException;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.dto.request.auth.AuthRequest;
import ru.ricardocraft.client.dto.request.auth.password.AuthOAuthPassword;
import ru.ricardocraft.client.dto.request.update.ProfilesRequest;
import ru.ricardocraft.client.ui.scenes.AbstractScene;
import ru.ricardocraft.client.client.OfflineRequestService;
import ru.ricardocraft.client.client.StdWebSocketService;
import ru.ricardocraft.client.service.client.BasicLauncherEventHandler;
import ru.ricardocraft.client.service.client.ClientLauncherMethods;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.ui.impl.GuiEventHandler;
import ru.ricardocraft.client.service.runtime.client.DirBridge;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.base.helper.LogHelper;
import ru.ricardocraft.client.base.helper.SecurityHelper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Configuration
public class RequestConfiguration {

    @Bean
    public RequestService requestService(LauncherConfig config,
                                         GuiObjectsContainer guiObjectsContainer,
                                         SettingsManager settingsManager,
                                         AuthService authService) {
        RequestService requestService;
        if (!Request.isAvailable()) {
            String address = config.address;
            LogHelper.debug("Start async connection to %s", address);
            RequestService service;
            try {
                StdWebSocketService stdWebSocketService = new StdWebSocketService(address);
                CompletableFuture<StdWebSocketService> future = new CompletableFuture<>();
                stdWebSocketService.openAsync(() -> {
                    future.complete(stdWebSocketService);
                    JVMHelper.RUNTIME.addShutdownHook(new Thread(() -> {
                        try {
                            stdWebSocketService.close();
                        } catch (InterruptedException e) {
                            LogHelper.error(e);
                        }
                    }));
                }, future::completeExceptionally);

                service = future.get();
            } catch (Throwable e) {
                if (LogHelper.isDebugEnabled()) {
                    LogHelper.error(e);
                }
                LogHelper.warning("Launcher in offline mode");
                OfflineRequestService offlineService = new OfflineRequestService();
                ClientLauncherMethods.applyBasicOfflineProcessors(offlineService);
                offlineService.registerRequestProcessor(
                        AuthRequest.class, (r) -> {
                            var permissions = new ClientPermissions();
                            String login = r.login;
                            if (login == null && r.password instanceof AuthOAuthPassword oAuthPassword) {
                                login = oAuthPassword.accessToken;
                            }
                            if (login == null) {
                                login = "Player";
                            }
                            return new AuthRequestEvent(
                                    permissions,
                                    new PlayerProfile(
                                            UUID.nameUUIDFromBytes(login.getBytes(StandardCharsets.UTF_8)), login,
                                            new HashMap<>(), new HashMap<>()), SecurityHelper.randomStringToken(), "", null,
                                    new AuthRequestEvent.OAuthRequestEvent(
                                            login, null, 0));
                        });
                offlineService.registerRequestProcessor(
                        ProfilesRequest.class, (r) -> {
                            List<ClientProfile> profileList =
                                    settingsManager.getRuntimeSettings().profiles.stream()
                                            .filter(profile -> Files.exists(
                                                    DirBridge.dirUpdates.resolve(profile.getDir()))
                                                    && Files.exists(DirBridge.dirUpdates.resolve(
                                                    profile.getAssetDir())))
                                            .collect(Collectors.toList());
                            return new ProfilesRequestEvent(profileList);
                        });
                service = offlineService;
            }
            Request.setRequestService(service);
            if (service instanceof StdWebSocketService) {
                ((StdWebSocketService) service).reconnectCallback = () ->
                {
                    LogHelper.debug("WebSocket connect closed. Try reconnect");
                    try {
                        Request.reconnect();
                    } catch (Exception e) {
                        LogHelper.error(e);
                        throw new RequestException("Connection failed", e);
                    }
                };
            }
        }
        Request.startAutoRefresh();
        Request.getRequestService().registerEventHandler(new BasicLauncherEventHandler());

        GuiEventHandler guiEventHandler = new GuiEventHandler(authService, settingsManager) {
            @Override
            protected AbstractScene getCurrentScene() {
                return guiObjectsContainer.getCurrentScene();
            }
        };

        requestService = Request.getRequestService();
        requestService.registerEventHandler(guiEventHandler);
        return requestService;
    }
}
