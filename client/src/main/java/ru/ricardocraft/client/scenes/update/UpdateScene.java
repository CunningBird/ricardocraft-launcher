package ru.ricardocraft.client.scenes.update;

import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.base.profiles.optional.OptionalView;
import ru.ricardocraft.client.base.request.RequestService;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.core.hasher.FileNameMatcher;
import ru.ricardocraft.client.core.hasher.HashedDir;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.nio.file.Path;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

@Component
@Scope("prototype")
public class UpdateScene extends AbstractScene {
    private ProgressBar progressBar;
    private Label speed;
    private Label volume;
    private TextArea logOutput;
    private Button cancel;
    private Label speedtext;
    private Label speederr;
    private Pane speedon;

    private final GuiModuleConfig guiModuleConfig;
    private final RequestService service;

    private VisualDownloader downloader;
    private volatile DownloadStatus downloadStatus = DownloadStatus.COMPLETE;

    public UpdateScene(LauncherConfig config,
                       GuiModuleConfig guiModuleConfig,
                       RequestService service,
                       AuthService authService,
                       LaunchService launchService,
                       SettingsManager settingsManager) {
        super("scenes/update/update.fxml", JavaFXApplication.getInstance(), config, guiModuleConfig, authService, launchService, settingsManager);
        this.guiModuleConfig = guiModuleConfig;
        this.service = service;
    }

    @Override
    protected void doInit() {
        progressBar = LookupHelper.lookup(layout, "#progress");
        speed = LookupHelper.lookup(layout, "#speed");
        speederr = LookupHelper.lookup(layout, "#speedErr");
        speedon = LookupHelper.lookup(layout, "#speedOn");
        speedtext = LookupHelper.lookup(layout, "#speed-text");
        cancel = LookupHelper.lookup(layout, "#cancel");
        volume = LookupHelper.lookup(layout, "#volume");
        logOutput = LookupHelper.lookup(layout, "#outputUpdate");
        downloader = new VisualDownloader(service, guiModuleConfig, launchService, progressBar, speed, volume, this::errorHandle,
                (log) -> contextHelper.runInFxThread(() -> addLog(log)), this::onUpdateStatus);
        LookupHelper.<ButtonBase>lookup(layout, "#cancel").setOnAction((e) -> {
            if (downloadStatus == DownloadStatus.DOWNLOAD && downloader.isDownload()) {
                downloader.cancel();
            } else if (downloadStatus == DownloadStatus.ERROR || downloadStatus == DownloadStatus.COMPLETE) {
                try {
                    switchToBackScene();
                } catch (Exception exception) {
                    errorHandle(exception);
                }
            }
        });
    }

    private void onUpdateStatus(DownloadStatus newStatus) {
        this.downloadStatus = newStatus;
        LogHelper.debug("Update download status: %s", newStatus.toString());
    }

    public void sendUpdateAssetRequest(String dirName, Path dir, FileNameMatcher matcher, boolean digest,
                                       String assetIndex, boolean test, Consumer<HashedDir> onSuccess) {
        downloader.sendUpdateAssetRequest(dirName, dir, matcher, digest, assetIndex, test, onSuccess);
    }

    public void sendUpdateRequest(String dirName, Path dir, FileNameMatcher matcher, boolean digest, OptionalView view,
                                  boolean optionalsEnabled, boolean test, Consumer<HashedDir> onSuccess) {
        downloader.sendUpdateRequest(dirName, dir, matcher, digest, view, optionalsEnabled, test, onSuccess);
    }

    public void addLog(String string) {
        LogHelper.dev("Update event %s", string);
        logOutput.appendText(string.concat("\n"));
    }

    @Override
    public void reset() {
        progressBar.progressProperty().setValue(0);
        logOutput.setText("");
        volume.setText("");
        speed.setText("0");
        progressBar.getStyleClass().removeAll("progress");
        speederr.setVisible(false);
        speedon.setVisible(true);
    }

    @Override
    public void errorHandle(Throwable e) {
        if (e instanceof CompletionException) {
            e = e.getCause();
        }
        addLog("Exception %s: %s".formatted(e.getClass(), e.getMessage() == null ? "" : e.getMessage()));
        progressBar.getStyleClass().add("progressError");
        speederr.setVisible(true);
        speedon.setVisible(false);
        LogHelper.error(e);
    }

    @Override
    public boolean isDisableReturnBack() {
        return true;
    }

    @Override
    public String getName() {
        return "update";
    }

    public enum DownloadStatus {
        ERROR, HASHING, REQUEST, DOWNLOAD, COMPLETE, DELETE
    }
}
