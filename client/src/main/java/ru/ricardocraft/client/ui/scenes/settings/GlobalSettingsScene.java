package ru.ricardocraft.client.ui.scenes.settings;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import ru.ricardocraft.client.configuration.GuiObjectsContainer;
import ru.ricardocraft.client.ui.impl.AbstractVisualComponent;
import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.base.helper.LookupHelper;
import ru.ricardocraft.client.service.runtime.client.DirBridge;
import ru.ricardocraft.client.service.SettingsManager;
import ru.ricardocraft.client.ui.scenes.console.ConsoleScene;
import ru.ricardocraft.client.ui.scenes.settings.components.LanguageConverter;
import ru.ricardocraft.client.ui.scenes.settings.components.ThemeConverter;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.JavaService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.ui.stage.ConsoleStage;
import ru.ricardocraft.client.base.helper.IOHelper;
import ru.ricardocraft.client.base.helper.LogHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static ru.ricardocraft.client.base.helper.EnFSHelper.isThemeSupport;

public abstract class GlobalSettingsScene extends BaseSettingsScene {

    private final GuiObjectsContainer guiObjectsContainer;
    private final RuntimeSettings runtimeSettings;
    private final SettingsManager settingsManager;
    private final JavaService javaService;

    private final ConsoleStage consoleStage;

    public GlobalSettingsScene(LauncherConfig config,
                               GuiObjectsContainer guiObjectsContainer,
                               GuiModuleConfig guiModuleConfig,
                               SettingsManager settingsManager,
                               AuthService authService,
                               LaunchService launchService,
                               JavaService javaService) {
        super("scenes/settings/globalsettings.fxml", config, guiModuleConfig, authService, launchService, settingsManager);
        this.runtimeSettings = settingsManager.getRuntimeSettings();
        this.guiObjectsContainer = guiObjectsContainer;
        this.settingsManager = settingsManager;
        this.javaService = javaService;

        consoleStage = new ConsoleStage(config) {
            @Override
            protected AbstractVisualComponent getByName(String name) {
                return GlobalSettingsScene.this.getByName(name);
            }
        };
    }

    abstract protected ConsoleScene getConsoleScene();

    abstract protected AbstractVisualComponent getByName(String name);

    @Override
    public String getName() {
        return "globalsettings";
    }

    @Override
    protected void doInit() {
        super.doInit();

        registerLanguageSelectorComponent(componentList);
        registerThemeSelectorComponent(componentList);

        LookupHelper.<ButtonBase>lookup(header, "#controls", "#console").setOnAction((e) -> {
            try {
                if (consoleStage.isNullScene())
                    consoleStage.setScene(getConsoleScene(), true);
                consoleStage.show();
            } catch (Exception ex) {
                errorHandle(ex);
            }
        });
        Hyperlink updateDirLink = LookupHelper.lookup(componentList, "#folder", "#path");
        String directoryUpdates = DirBridge.dirUpdates.toAbsolutePath().toString();
        updateDirLink.setText(directoryUpdates);
        if (updateDirLink.getTooltip() != null) {
            updateDirLink.getTooltip().setText(directoryUpdates);
        }
        updateDirLink.setOnAction((e) -> guiObjectsContainer.openURL(directoryUpdates));
        LookupHelper.<ButtonBase>lookup(componentList, "#changeDir").setOnAction((e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(launchService.getTranslation("runtime.scenes.settings.dirTitle"));
            directoryChooser.setInitialDirectory(DirBridge.dir.toFile());
            File choose = directoryChooser.showDialog(guiObjectsContainer.getMainStage().getStage());
            if (choose == null) return;
            Path newDir = choose.toPath().toAbsolutePath();
            try {
                DirBridge.move(newDir);
            } catch (IOException ex) {
                errorHandle(ex);
            }
            runtimeSettings.updatesDirPath = newDir.toString();
            runtimeSettings.updatesDir = newDir;
            String oldDir = DirBridge.dirUpdates.toString();
            DirBridge.dirUpdates = newDir;
            for (ClientProfile profile : settingsManager.getProfiles()) {
                RuntimeSettings.ProfileSettings settings = settingsManager.getProfileSettings(profile);
                if (settings.javaPath != null && settings.javaPath.startsWith(oldDir)) {
                    settings.javaPath = newDir.toString().concat(settings.javaPath.substring(oldDir.length()));
                }
            }
            javaService.update();
            updateDirLink.setText(runtimeSettings.updatesDirPath);
        });
        LookupHelper.<ButtonBase>lookupIfPossible(layout, "#deleteDir").ifPresent(a -> a.setOnAction(
                (e) -> launchService.showApplyDialog(
                        launchService.getTranslation("runtime.scenes.settings.deletedir.header"),
                        launchService.getTranslation("runtime.scenes.settings.deletedir.description"), () -> {
                            LogHelper.debug("Delete dir: %s", DirBridge.dirUpdates);
                            try {
                                IOHelper.deleteDir(DirBridge.dirUpdates, false);
                            } catch (IOException ex) {
                                LogHelper.error(ex);
                                launchService.createNotification(
                                        launchService.getTranslation("runtime.scenes.settings.deletedir.fail.header"),
                                        launchService.getTranslation("runtime.scenes.settings.deletedir.fail.description"));
                            }
                        }, () -> {
                        }, true)));
        LookupHelper.<ButtonBase>lookupIfPossible(layout, "#back").ifPresent(a -> a.setOnAction((e) -> {
            try {
                switchToBackScene();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        }));
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        RuntimeSettings.GlobalSettings settings = runtimeSettings.globalSettings;
        add("PrismVSync", settings.prismVSync, (value) -> settings.prismVSync = value, false);
        add("DebugAllClients", settings.debugAllClients, (value) -> settings.debugAllClients = value, false);
    }

    private void registerThemeSelectorComponent(Pane layout) {
        RuntimeSettings runtimeSettings = settingsManager.getRuntimeSettings();
        ComboBox<RuntimeSettings.LAUNCHER_THEME> comboBox = LookupHelper.lookup(layout, "#themeCombo");
        comboBox.getItems().clear();
        comboBox.setConverter(new ThemeConverter(launchService));
        if (isThemeSupport()) {
            for (var e : RuntimeSettings.LAUNCHER_THEME.values()) {
                comboBox.getItems().add(e);
            }
        } else {
            comboBox.getItems().add(RuntimeSettings.LAUNCHER_THEME.COMMON);
        }
        comboBox.getSelectionModel().select(Objects.requireNonNullElse(runtimeSettings.theme,
                RuntimeSettings.LAUNCHER_THEME.COMMON));
        comboBox.setOnAction(e -> {
            RuntimeSettings.LAUNCHER_THEME theme = comboBox.getValue();
            if (theme == null || (theme == RuntimeSettings.LAUNCHER_THEME.COMMON && runtimeSettings.theme == null))
                return;
            if (theme == runtimeSettings.theme) return;
            runtimeSettings.theme = theme;
            try {
                guiObjectsContainer.reload();
            } catch (Exception ex) {
                LogHelper.error(ex);
            }
        });
    }

    private void registerLanguageSelectorComponent(Pane layout) {
        ComboBox<RuntimeSettings.LAUNCHER_LOCALE> comboBox = LookupHelper.lookup(layout, "#languageCombo");
        comboBox.getItems().clear();
        comboBox.setConverter(new LanguageConverter(launchService));
        for (var e : RuntimeSettings.LAUNCHER_LOCALE.values()) {
            comboBox.getItems().add(e);
        }
        comboBox.getSelectionModel().select(Objects.requireNonNullElse(settingsManager.getRuntimeSettings().locale, RuntimeSettings.LAUNCHER_LOCALE.ENGLISH));
        comboBox.setOnAction(e -> {
            RuntimeSettings.LAUNCHER_LOCALE locale = comboBox.getValue();
            if (locale == null) return;
            if (locale == settingsManager.getRuntimeSettings().locale) return;
            try {
                launchService.updateLocaleResources(locale.name);
                settingsManager.getRuntimeSettings().locale = locale;
                guiObjectsContainer.reload();
            } catch (Exception ex) {
                LogHelper.error(ex);
            }
        });
    }
}
