package ru.ricardocraft.client.scenes.servermenu;

import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.components.ServerButton;
import ru.ricardocraft.client.components.UserBlock;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.runtime.client.ServerPinger;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;
import ru.ricardocraft.client.service.ProfilesService;
import ru.ricardocraft.client.utils.helper.CommonHelper;

import java.io.IOException;
import java.util.*;

public class ServerMenuScene extends AbstractScene implements SceneSupportUserBlock {
    private List<ClientProfile> lastProfiles;
    private UserBlock userBlock;

    private final GuiModuleConfig guiModuleConfig;
    private final SettingsManager settingsManager;
    private final ProfilesService profilesService;
    private final PingService pingService;
    private final SkinManager skinManager;

    public ServerMenuScene(JavaFXApplication application,
                           LauncherConfig config,
                           GuiModuleConfig guiModuleConfig,
                           SettingsManager settingsManager,
                           AuthService authService,
                           SkinManager skinManager,
                           LaunchService launchService,
                           ProfilesService profilesService,
                           PingService pingService) {
        super("scenes/servermenu/servermenu.fxml", application, config, guiModuleConfig, authService, launchService);
        this.guiModuleConfig = guiModuleConfig;
        this.settingsManager = settingsManager;
        this.profilesService = profilesService;
        this.pingService = pingService;
        this.skinManager = skinManager;
    }

    @Override
    public void doInit() {
        this.userBlock = new UserBlock(layout, authService, skinManager, launchService, new SceneAccessor());
        LookupHelper.<ButtonBase>lookup(header, "#controls", "#settings").setOnAction((e) -> {
            try {
                switchScene(application.gui.globalSettingsScene);
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });
        ScrollPane scrollPane = LookupHelper.lookup(layout, "#servers");
        scrollPane.setOnScroll(e -> {
            double widthContent = scrollPane.getWidth();
            double offset = (widthContent * 0.15) / (scrollPane.getContent().getBoundsInLocal().getWidth() - widthContent) * Math.signum(e.getDeltaY());
            scrollPane.setHvalue(scrollPane.getHvalue() - offset);
        });
        reset();
        isResetOnShow = true;
    }

    static class ServerButtonCache {
        public ServerButton serverButton;
        public int position;
    }

    @Override
    public void reset() {
        if (lastProfiles == profilesService.getProfiles()) return;
        lastProfiles = profilesService.getProfiles();
        Map<ClientProfile, ServerButtonCache> serverButtonCacheMap = new LinkedHashMap<>();

        List<ClientProfile> profiles = new ArrayList<>(lastProfiles);
        profiles.sort(Comparator.comparingInt(ClientProfile::getSortIndex).thenComparing(ClientProfile::getTitle));
        int position = 0;
        for (ClientProfile profile : profiles) {
            ServerButtonCache cache = new ServerButtonCache();
            cache.serverButton = ServerButton.createServerButton(
                    application,
                    guiModuleConfig,
                    launchService,
                    pingService,
                    profile
            );
            cache.position = position;
            serverButtonCacheMap.put(profile, cache);
            position++;
        }
        ScrollPane scrollPane = LookupHelper.lookup(layout, "#servers");
        HBox serverList = (HBox) scrollPane.getContent();
        serverList.setSpacing(20);
        serverList.getChildren().clear();
        pingService.clear();
        serverButtonCacheMap.forEach((profile, serverButtonCache) -> {
            EventHandler<? super MouseEvent> handle = (event) -> {
                if (!event.getButton().equals(MouseButton.PRIMARY)) return;
                changeServer(profile);
                try {
                    switchScene(application.gui.serverInfoScene);
                    application.gui.serverInfoScene.reset();
                } catch (Exception e) {
                    errorHandle(e);
                }
            };
            serverButtonCache.serverButton.addTo(serverList, serverButtonCache.position);
            serverButtonCache.serverButton.setOnMouseClicked(handle);
        });
        CommonHelper.newThread("ServerPinger", true, () -> {
            for (ClientProfile profile : lastProfiles) {
                for (ClientProfile.ServerProfile serverProfile : profile.getServers()) {
                    if (!serverProfile.socketPing || serverProfile.serverAddress == null) continue;
                    try {
                        ServerPinger pinger = new ServerPinger(serverProfile, profile.getVersion());
                        ServerPinger.Result result = pinger.ping();
                        contextHelper.runInFxThread(
                                () -> pingService.addReport(serverProfile.name, result));
                    } catch (IOException ignored) {
                    }
                }
            }
        }).start();
        userBlock.reset();
    }

    @Override
    public UserBlock getUserBlock() {
        return userBlock;
    }

    @Override
    public String getName() {
        return "serverMenu";
    }

    private void changeServer(ClientProfile profile) {
        profilesService.setProfile(profile);
        settingsManager.getRuntimeSettings().lastProfile = profile.getUUID();
    }
}
