package ru.ricardocraft.client.ui.overlays;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.ricardocraft.client.ui.DesignConstants;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.service.launch.SkinManager;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.base.utils.JavaFxUtils;
import ru.ricardocraft.client.base.helper.LogHelper;

public class WelcomeOverlay extends AbstractOverlay {
    private Image originalImage;

    private final AuthService authService;
    private final SkinManager skinManager;

    public WelcomeOverlay(GuiModuleConfig guiModuleConfig,
                          AuthService authService,
                          SkinManager skinManager,
                          LaunchService launchService) {
        super("overlay/welcome/welcome.fxml", guiModuleConfig, launchService);
        this.authService = authService;
        this.skinManager = skinManager;
    }

    @Override
    public String getName() {
        return "welcome";
    }

    @Override
    protected void doInit() {
        reset();
    }

    @Override
    public void reset() {
        LookupHelper.<Label>lookupIfPossible(layout, "#playerName")
                .ifPresent((e) -> e.setText(authService.getUsername()));
        LookupHelper.<ImageView>lookupIfPossible(layout, "#playerHead").ifPresent((h) -> {
            try {
                JavaFxUtils.setStaticRadius(h, DesignConstants.AVATAR_IMAGE_RADIUS);
                Image image = skinManager.getScaledFxSkinHead(
                        authService.getUsername(), (int) h.getFitWidth(), (int) h.getFitHeight());
                if (image != null) {
                    if (originalImage == null) {
                        originalImage = h.getImage();
                    }
                    h.setImage(image);
                } else if (originalImage != null) {
                    h.setImage(originalImage);
                }
            } catch (Throwable e) {
                LogHelper.warning("Skin head error");
            }
        });
    }
}
