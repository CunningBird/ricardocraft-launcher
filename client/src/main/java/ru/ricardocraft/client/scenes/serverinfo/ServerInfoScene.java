package ru.ricardocraft.client.scenes.serverinfo;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.components.ServerButton;
import ru.ricardocraft.client.components.UserBlock;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.debug.DebugScene;
import ru.ricardocraft.client.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.scenes.options.OptionsScene;
import ru.ricardocraft.client.scenes.settings.SettingsScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;
import ru.ricardocraft.client.utils.helper.LogHelper;

@Component
@Scope("prototype")
public class ServerInfoScene extends AbstractScene implements SceneSupportUserBlock {
    private UserBlock userBlock;

    private final GuiModuleConfig guiModuleConfig;
    private final SettingsManager settingsManager;
    private final SkinManager skinManager;
    private final PingService pingService;

    public ServerInfoScene(LauncherConfig config,
                           GuiModuleConfig guiModuleConfig,
                           SettingsManager settingsManager,
                           AuthService authService,
                           SkinManager skinManager,
                           LaunchService launchService,
                           PingService pingService) {
        super("scenes/serverinfo/serverinfo.fxml", JavaFXApplication.getInstance(), config, guiModuleConfig, authService, launchService, settingsManager);
        this.guiModuleConfig = guiModuleConfig;
        this.settingsManager = settingsManager;
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
                if (settingsManager.getProfile() == null) return;
                OptionsScene optionsScene = (OptionsScene) application.gui.getByName("options");
                switchScene(optionsScene);
                optionsScene.reset();
            } catch (Exception ex) {
                errorHandle(ex);
            }
        });
        LookupHelper.<ButtonBase>lookup(header, "#controls", "#settings").setOnAction((e) -> {
            try {
                SettingsScene settingsScene = (SettingsScene) application.gui.getByName("settings");
                switchScene(settingsScene);
                settingsScene.reset();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });
        reset();
    }

    @Override
    public void reset() {
        ClientProfile profile = settingsManager.getProfile();
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
                        DebugScene debugScene = (DebugScene) application.gui.getByName("debug");
                        switchScene(debugScene);
                        debugScene.onClientInstance(clientInstance);
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
