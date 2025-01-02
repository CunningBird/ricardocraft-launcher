package ru.ricardocraft.client.ui.impl;

import javafx.application.Platform;
import ru.ricardocraft.client.dto.RequestEvent;
import ru.ricardocraft.client.dto.response.AuthRequestEvent;
import ru.ricardocraft.client.dto.response.ProfilesRequestEvent;
import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.dto.request.RequestService;
import ru.ricardocraft.client.dto.response.WebSocketEvent;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.ui.scenes.AbstractScene;
import ru.ricardocraft.client.ui.scenes.login.AuthFlow;
import ru.ricardocraft.client.ui.scenes.login.LoginScene;
import ru.ricardocraft.client.ui.scenes.options.OptionsScene;
import ru.ricardocraft.client.ui.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.ui.scenes.servermenu.ServerMenuScene;
import ru.ricardocraft.client.ui.scenes.settings.SettingsScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.base.helper.LogHelper;

import java.util.UUID;

public abstract class GuiEventHandler implements RequestService.EventHandler {

    private final SettingsManager settingsManager;
    private final AuthService authService;

    public GuiEventHandler(AuthService authService, SettingsManager settingsManager) {
        this.authService = authService;
        this.settingsManager = settingsManager;
    }

    @Override
    public <T extends WebSocketEvent> boolean eventHandle(T event) {
        LogHelper.dev("Processing event %s", event.getType());
        if (event instanceof RequestEvent requestEvent) {
            if (!requestEvent.requestUUID.equals(RequestEvent.eventUUID)) return false;
        }
        try {
            if (event instanceof AuthRequestEvent authRequestEvent) {
                boolean isNextScene = getCurrentScene() instanceof LoginScene; //TODO: FIX
                LogHelper.dev("Receive auth event. Send next scene %s", isNextScene ? "true" : "false");
                authService.setAuthResult(null, authRequestEvent);
                if (isNextScene) {
                    Platform.runLater(() -> {
                        try {
                            ((LoginScene) getCurrentScene()).onSuccessLogin(
                                    new AuthFlow.SuccessAuth(authRequestEvent,
                                                             authRequestEvent.playerProfile != null ? authRequestEvent.playerProfile.username : null,
                                                             null));
                        } catch (Throwable e) {
                            LogHelper.error(e);
                        }
                    });
                }
            }
            if (event instanceof ProfilesRequestEvent profilesRequestEvent) {
                settingsManager.setProfilesResult(profilesRequestEvent);
                if (settingsManager.getProfile() != null) {
                    UUID profileUUID = settingsManager.getProfile().getUUID();
                    for (ClientProfile profile : settingsManager.getProfiles()) {
                        if (profile.getUUID().equals(profileUUID)) {
                            settingsManager.setProfile(profile);
                            break;
                        }
                    }
                }
                AbstractScene scene = getCurrentScene();
                if (scene instanceof ServerMenuScene
                        || scene instanceof ServerInfoScene
                        || scene instanceof SettingsScene | scene instanceof OptionsScene) {
                    scene.contextHelper.runInFxThread(scene::reset);
                }
            }
        } catch (Throwable e) {
            LogHelper.error(e);
        }
        return false;
    }

    abstract protected AbstractScene getCurrentScene();
}
