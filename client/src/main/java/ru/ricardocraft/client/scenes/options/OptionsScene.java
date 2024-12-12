package ru.ricardocraft.client.scenes.options;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.profiles.ClientProfile;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.components.ServerButton;
import ru.ricardocraft.client.components.UserBlock;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.scenes.serverinfo.ServerInfoScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;

@Component
@Scope("prototype")
public class OptionsScene extends AbstractScene implements SceneSupportUserBlock {
    private OptionsTab optionsTab;
    private UserBlock userBlock;

    private final GuiModuleConfig guiModuleConfig;
    private final PingService pingService;
    private final SkinManager skinManager;

    public OptionsScene(LauncherConfig config,
                        GuiModuleConfig guiModuleConfig,
                        AuthService authService,
                        SkinManager skinManager,
                        LaunchService launchService,
                        PingService pingService,
                        SettingsManager settingsManager) {
        super("scenes/options/options.fxml", JavaFXApplication.getInstance(), config, guiModuleConfig, authService, launchService, settingsManager);
        this.guiModuleConfig = guiModuleConfig;
        this.pingService = pingService;
        this.skinManager = skinManager;
    }

    @Override
    protected void doInit() {
        this.userBlock = new UserBlock(layout, authService, skinManager, launchService, new SceneAccessor());
        optionsTab = new OptionsTab(launchService, LookupHelper.lookup(layout, "#tabPane"));
    }

    @Override
    public void reset() {
        Pane serverButtonContainer = LookupHelper.lookup(layout, "#serverButton");
        serverButtonContainer.getChildren().clear();
        ClientProfile profile = settingsManager.getProfile();
        ServerButton serverButton = ServerButton.createServerButton(
                application,
                guiModuleConfig,
                launchService,
                pingService,
                profile
        );
        serverButton.addTo(serverButtonContainer);
        serverButton.enableSaveButton(null, (e) -> {
            try {
                settingsManager.setOptionalView(profile, optionsTab.getOptionalView());
                switchScene((ServerInfoScene) application.gui.getByName("serverinfo"));
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });
        serverButton.enableResetButton(null, (e) -> {
            optionsTab.clear();
            settingsManager.setOptionalView(profile, new OptionalView(profile));
            optionsTab.addProfileOptionals(settingsManager.getOptionalView());
        });
        optionsTab.clear();
        LookupHelper.<Button>lookupIfPossible(layout, "#back").ifPresent(x -> x.setOnAction((e) -> {
            try {
                switchToBackScene();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        }));
        optionsTab.addProfileOptionals(settingsManager.getOptionalView());
        userBlock.reset();
    }

    @Override
    public String getName() {
        return "options";
    }

    @Override
    public UserBlock getUserBlock() {
        return userBlock;
    }
}
