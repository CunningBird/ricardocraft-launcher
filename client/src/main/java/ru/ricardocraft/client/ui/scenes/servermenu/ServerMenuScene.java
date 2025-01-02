package ru.ricardocraft.client.ui.scenes.servermenu;

import javafx.event.EventHandler;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ru.ricardocraft.client.ui.overlays.UploadAssetOverlay;
import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.ui.components.ServerButton;
import ru.ricardocraft.client.ui.components.UserBlock;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.service.launch.SkinManager;
import ru.ricardocraft.client.service.runtime.client.ServerPinger;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.ui.scenes.AbstractScene;
import ru.ricardocraft.client.ui.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.ui.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.ui.scenes.settings.GlobalSettingsScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;
import ru.ricardocraft.client.base.helper.CommonHelper;

import java.io.IOException;
import java.util.*;

public abstract class ServerMenuScene extends AbstractScene implements SceneSupportUserBlock {
    private List<ClientProfile> lastProfiles;
    private UserBlock userBlock;

    private final GuiModuleConfig guiModuleConfig;
    private final SettingsManager settingsManager;
    private final PingService pingService;
    private final SkinManager skinManager;

    public ServerMenuScene(LauncherConfig config,
                           GuiModuleConfig guiModuleConfig,
                           SettingsManager settingsManager,
                           AuthService authService,
                           SkinManager skinManager,
                           LaunchService launchService,
                           PingService pingService) {
        super("scenes/servermenu/servermenu.fxml", config, guiModuleConfig, authService, launchService, settingsManager);
        this.guiModuleConfig = guiModuleConfig;
        this.settingsManager = settingsManager;
        this.pingService = pingService;
        this.skinManager = skinManager;
    }

    abstract protected GlobalSettingsScene getGlobalSettingsScene();

    abstract protected ServerInfoScene getServerInfoScene();

    abstract protected UploadAssetOverlay getUploadAsset();

    @Override
    public void doInit() {
        this.userBlock = new UserBlock(layout, authService, skinManager, launchService, new SceneAccessor()) {
            @Override
            protected UploadAssetOverlay getUploadAsset() {
                return ServerMenuScene.this.getUploadAsset();
            }
        };
        LookupHelper.<ButtonBase>lookup(header, "#controls", "#settings").setOnAction((e) -> {
            try {
                switchScene(getGlobalSettingsScene());
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

    @Override
    public void reset() {
        if (lastProfiles == settingsManager.getProfiles()) return;
        lastProfiles = settingsManager.getProfiles();
        Map<ClientProfile, ServerButtonCache> serverButtonCacheMap = new LinkedHashMap<>();

        List<ClientProfile> profiles = new ArrayList<>(lastProfiles);
        profiles.sort(Comparator.comparingInt(ClientProfile::getSortIndex).thenComparing(ClientProfile::getTitle));
        int position = 0;
        for (ClientProfile profile : profiles) {
            ServerButtonCache cache = new ServerButtonCache();
            cache.serverButton = ServerButton.createServerButton(
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
                    ServerInfoScene serverInfoScene = getServerInfoScene();
                    switchScene(serverInfoScene);
                    serverInfoScene.reset();
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
        settingsManager.setProfile(profile);
        settingsManager.getRuntimeSettings().lastProfile = profile.getUUID();
    }

    static class ServerButtonCache {
        public ServerButton serverButton;
        public int position;
    }
}
