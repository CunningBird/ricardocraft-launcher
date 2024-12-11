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
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class ServerInfoScene extends AbstractScene implements SceneSupportUserBlock {
    private UserBlock userBlock;

    public ServerInfoScene(JavaFXApplication application) {
        super("scenes/serverinfo/serverinfo.fxml", application);
    }

    @Override
    protected void doInit() {
        this.userBlock = new UserBlock(layout, new SceneAccessor());
        LookupHelper.<Button>lookup(layout, "#back").setOnAction((e) -> {
            try {
                switchToBackScene();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });

        LookupHelper.<ButtonBase>lookup(header, "#controls", "#clientSettings").setOnAction((e) -> {
            try {
                if (application.profilesService.getProfile() == null) return;
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
        ClientProfile profile = application.profilesService.getProfile();
        LookupHelper.<Label>lookupIfPossible(layout, "#serverName").ifPresent((e) -> e.setText(profile.getTitle()));
        LookupHelper.<ScrollPane>lookupIfPossible(layout, "#serverDescriptionPane").ifPresent((e) -> {
            var label = (Label) e.getContent();
            label.setText(profile.getInfo());
        });
        Pane serverButtonContainer = LookupHelper.lookup(layout, "#serverButton");
        serverButtonContainer.getChildren().clear();
        ServerButton serverButton = ServerButton.createServerButton(application, profile);
        serverButton.addTo(serverButtonContainer);
        serverButton.enableSaveButton(application.getTranslation("runtime.scenes.serverinfo.serverButton.game"),
                                      (e) -> runClient());
        this.userBlock.reset();
    }

    private void runClient() {
        application.launchService.launchClient().thenAccept((clientInstance -> {
            if (application.runtimeSettings.globalSettings.debugAllClients || clientInstance.getSettings().debug) {
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
