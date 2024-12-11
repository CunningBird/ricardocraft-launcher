package ru.ricardocraft.client.components;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.events.request.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.client.base.request.cabinet.AssetUploadInfoRequest;
import ru.ricardocraft.client.config.DesignConstants;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.utils.JavaFxUtils;
import ru.ricardocraft.client.utils.helper.LogHelper;

public class UserBlock {
    private final JavaFXApplication application;
    private final Pane layout;
    private final AbstractScene.SceneAccessor sceneAccessor;
    private final ImageView avatar;
    private final Image originalAvatarImage;

    public UserBlock(Pane layout, AbstractScene.SceneAccessor sceneAccessor) {
        this.application = sceneAccessor.getApplication();
        this.layout = layout;
        this.sceneAccessor = sceneAccessor;
        avatar = LookupHelper.lookup(layout, "#avatar");
        originalAvatarImage = avatar.getImage();
        LookupHelper.<ImageView>lookupIfPossible(layout, "#avatar").ifPresent((h) -> {
            try {
                JavaFxUtils.setStaticRadius(h, DesignConstants.AVATAR_IMAGE_RADIUS);
                h.setImage(originalAvatarImage);
            } catch (Throwable e) {
                LogHelper.warning("Skin head error");
            }
        });
        reset();
    }

    public void reset() {
        LookupHelper.<Label>lookupIfPossible(layout, "#nickname")
                    .ifPresent((e) -> e.setText(application.authService.getUsername()));
        LookupHelper.<Label>lookupIfPossible(layout, "#role")
                    .ifPresent((e) -> e.setText(application.authService.getMainRole()));
        avatar.setImage(originalAvatarImage);
        resetAvatar();
        if(application.authService.isFeatureAvailable(GetAssetUploadUrlRequestEvent.FEATURE_NAME)) {
            LookupHelper.<Button>lookupIfPossible(layout, "#customization").ifPresent((h) -> {
                h.setVisible(true);
                h.setOnAction((a) -> sceneAccessor.processRequest(application.getTranslation("runtime.overlay.processing.text.uploadassetinfo"), new AssetUploadInfoRequest(), (info) -> sceneAccessor.runInFxThread(() -> sceneAccessor.showOverlay(application.gui.uploadAssetOverlay, (f) -> application.gui.uploadAssetOverlay.onAssetUploadInfo(info))), sceneAccessor::errorHandle, (e) -> {}));
            });
        }
    }

    public void resetAvatar() {
        if (avatar == null) {
            return;
        }
        JavaFxUtils.putAvatarToImageView(application, application.authService.getUsername(), avatar);
    }
}
