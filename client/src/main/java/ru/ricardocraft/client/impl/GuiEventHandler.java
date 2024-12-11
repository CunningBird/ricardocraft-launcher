package ru.ricardocraft.client.impl;

import javafx.application.Platform;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.events.RequestEvent;
import ru.ricardocraft.client.base.events.request.AuthRequestEvent;
import ru.ricardocraft.client.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.base.request.WebSocketEvent;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.login.AuthFlow;
import ru.ricardocraft.client.scenes.login.LoginScene;
import ru.ricardocraft.client.scenes.options.OptionsScene;
import ru.ricardocraft.client.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.scenes.servermenu.ServerMenuScene;
import ru.ricardocraft.client.scenes.settings.SettingsScene;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.util.UUID;

public class GuiEventHandler implements RequestService.EventHandler {
    private final JavaFXApplication application;

    public GuiEventHandler(JavaFXApplication application) {
        this.application = application;
    }

    @Override
    public <T extends WebSocketEvent> boolean eventHandle(T event) {
        LogHelper.dev("Processing event %s", event.getType());
        if (event instanceof RequestEvent requestEvent) {
            if (!requestEvent.requestUUID.equals(RequestEvent.eventUUID)) return false;
        }
        try {
            if (event instanceof AuthRequestEvent authRequestEvent) {
                boolean isNextScene = application.getCurrentScene() instanceof LoginScene; //TODO: FIX
                LogHelper.dev("Receive auth event. Send next scene %s", isNextScene ? "true" : "false");
                application.authService.setAuthResult(null, authRequestEvent);
                if (isNextScene) {
                    Platform.runLater(() -> {
                        try {
                            ((LoginScene) application.getCurrentScene()).onSuccessLogin(
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
                application.profilesService.setProfilesResult(profilesRequestEvent);
                if (application.profilesService.getProfile() != null) {
                    UUID profileUUID = application.profilesService.getProfile().getUUID();
                    for (ClientProfile profile : application.profilesService.getProfiles()) {
                        if (profile.getUUID().equals(profileUUID)) {
                            application.profilesService.setProfile(profile);
                            break;
                        }
                    }
                }
                AbstractScene scene = application.getCurrentScene();
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
}
