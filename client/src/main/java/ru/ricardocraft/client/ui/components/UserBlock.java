package ru.ricardocraft.client.ui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import ru.ricardocraft.client.dto.response.GetAssetUploadUrlRequestEvent;
import ru.ricardocraft.client.dto.request.cabinet.AssetUploadInfoRequest;
import ru.ricardocraft.client.ui.DesignConstants;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.service.launch.SkinManager;
import ru.ricardocraft.client.ui.overlays.UploadAssetOverlay;
import ru.ricardocraft.client.ui.scenes.AbstractScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.base.utils.JavaFxUtils;
import ru.ricardocraft.client.base.helper.LogHelper;

public abstract class UserBlock {

    private final AuthService authService;
    private final SkinManager skinManager;
    private final LaunchService launchService;

    private final Pane layout;
    private final AbstractScene.SceneAccessor sceneAccessor;
    private final ImageView avatar;
    private final Image originalAvatarImage;

    public UserBlock(Pane layout, AuthService authService, SkinManager skinManager, LaunchService launchService, AbstractScene.SceneAccessor sceneAccessor) {
        this.authService = authService;
        this.layout = layout;
        this.sceneAccessor = sceneAccessor;
        avatar = LookupHelper.lookup(layout, "#avatar");
        this.skinManager = skinManager;
        this.launchService = launchService;
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
                .ifPresent((e) -> e.setText(authService.getUsername()));
        LookupHelper.<Label>lookupIfPossible(layout, "#role")
                .ifPresent((e) -> e.setText(authService.getMainRole()));
        avatar.setImage(originalAvatarImage);
        resetAvatar();
        if (authService.isFeatureAvailable(GetAssetUploadUrlRequestEvent.FEATURE_NAME)) {
            LookupHelper.<Button>lookupIfPossible(layout, "#customization").ifPresent((h) -> {
                h.setVisible(true);
                h.setOnAction((a) -> sceneAccessor.processRequest(
                        launchService.getTranslation("runtime.overlay.processing.text.uploadassetinfo"),
                        new AssetUploadInfoRequest(),
                        (info) -> sceneAccessor.runInFxThread(() -> sceneAccessor.showOverlay(getUploadAsset(), (f) -> getUploadAsset().onAssetUploadInfo(info))),
                        sceneAccessor::errorHandle, (e) -> {
                        })
                );
            });
        }
    }

    public void resetAvatar() {
        if (avatar == null) {
            return;
        }
        JavaFxUtils.putAvatarToImageView(skinManager, authService.getUsername(), avatar);
    }

    abstract protected UploadAssetOverlay getUploadAsset();
}
