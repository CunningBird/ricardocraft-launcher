package ru.ricardocraft.client.scenes.serverinfo;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.components.ServerButton;
import ru.ricardocraft.client.components.UserBlock;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;
import ru.ricardocraft.client.service.ProfilesService;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class ServerInfoScene extends AbstractScene implements SceneSupportUserBlock {
    private UserBlock userBlock;

    private final GuiModuleConfig guiModuleConfig;
    private final SettingsManager settingsManager;
    private final ProfilesService profilesService;
    private final SkinManager skinManager;
    private final PingService pingService;

    public ServerInfoScene(JavaFXApplication application,
                           LauncherConfig config,
                           GuiModuleConfig guiModuleConfig,
                           SettingsManager settingsManager,
                           AuthService authService,
                           SkinManager skinManager,
                           LaunchService launchService,
                           ProfilesService profilesService,
                           PingService pingService) {
        super("scenes/serverinfo/serverinfo.fxml", application, config, guiModuleConfig, authService, launchService);
        this.guiModuleConfig = guiModuleConfig;
        this.settingsManager = settingsManager;
        this.profilesService = profilesService;
        this.skinManager = skinManager;
        this.pingService = pingService;
    }

    @Override
    protected void doInit() {
        this.userBlock = new UserBlock(layout, authService, skinManager, launchService, new SceneAccessor());
        LookupHelper.<Button>lookup(layout, "#back").setOnAction((e) -> {
            try {
                switchToBackScene();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });

        LookupHelper.<ButtonBase>lookup(header, "#controls", "#clientSettings").setOnAction((e) -> {
            try {
                if (profilesService.getProfile() == null) return;
                switchScene(application.gui.optionsScene);
                application.gui.optionsScene.reset();
            } catch (Exception ex) {
                errorHandle(ex);
            }
        });
        LookupHelper.<ButtonBase>lookup(header, "#controls", "#settings").setOnAction((e) -> {
            try {
                switchScene(application.gui.settingsScene);
                application.gui.settingsScene.reset();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });
        reset();
    }

    @Override
    public void reset() {
        ClientProfile profile = profilesService.getProfile();
        LookupHelper.<Label>lookupIfPossible(layout, "#serverName").ifPresent((e) -> e.setText(profile.getTitle()));
        LookupHelper.<ScrollPane>lookupIfPossible(layout, "#serverDescriptionPane").ifPresent((e) -> {
            var label = (Label) e.getContent();
            label.setText(profile.getInfo());
        });
        Pane serverButtonContainer = LookupHelper.lookup(layout, "#serverButton");
        serverButtonContainer.getChildren().clear();
        ServerButton serverButton = ServerButton.createServerButton(
                application,
                guiModuleConfig,
                launchService,
                pingService,
                profile
        );
        serverButton.addTo(serverButtonContainer);
        serverButton.enableSaveButton(launchService.getTranslation("runtime.scenes.serverinfo.serverButton.game"),
                (e) -> runClient());
        this.userBlock.reset();
    }

    private void runClient() {
        launchService.launchClient().thenAccept((clientInstance -> {
            if (settingsManager.getRuntimeSettings().globalSettings.debugAllClients || clientInstance.getSettings().debug) {
                contextHelper.runInFxThread(() -> {
                    try {
                        switchScene(application.gui.debugScene);
                        application.gui.debugScene.onClientInstance(clientInstance);
                    } catch (Exception ex) {
                        errorHandle(ex);
                    }
                });
            } else {
                clientInstance.start();
                clientInstance.getOnWriteParamsFuture().thenAccept((ok) -> {
                    LogHelper.info("Params write successful. Exit...");
                    Platform.exit();
                }).exceptionally((ex) -> {
                    contextHelper.runInFxThread(() -> errorHandle(ex));
                    return null;
                });
            }
        })).exceptionally((ex) -> {
            contextHelper.runInFxThread(() -> errorHandle(ex));
            return null;
        });
    }

    @Override
    public String getName() {
        return "serverinfo";
    }

    @Override
    public UserBlock getUserBlock() {
        return userBlock;
    }
}
