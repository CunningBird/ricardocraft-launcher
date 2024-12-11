package ru.ricardocraft.client.overlays;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.DesignConstants;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.utils.JavaFxUtils;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class WelcomeOverlay extends AbstractOverlay {
    private Image originalImage;
    public WelcomeOverlay(JavaFXApplication application) {
        super("overlay/welcome/welcome.fxml", application);
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
                    .ifPresent((e) -> e.setText(application.authService.getUsername()));
        LookupHelper.<ImageView>lookupIfPossible(layout, "#playerHead").ifPresent((h) -> {
            try {
                JavaFxUtils.setStaticRadius(h, DesignConstants.AVATAR_IMAGE_RADIUS);
                Image image = application.skinManager.getScaledFxSkinHead(
                        application.authService.getUsername(), (int) h.getFitWidth(), (int) h.getFitHeight());
                if (image != null) {
                    if(originalImage == null) {
                        originalImage = h.getImage();
                    }
                    h.setImage(image);
                } else if(originalImage != null) {
                    h.setImage(originalImage);
                }
            } catch (Throwable e) {
                LogHelper.warning("Skin head error");
            }
        });
    }
}
