package ru.ricardocraft.client.scenes.settings;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.components.ServerButton;
import ru.ricardocraft.client.components.UserBlock;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.launch.SkinManager;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.interfaces.SceneSupportUserBlock;
import ru.ricardocraft.client.scenes.settings.components.JavaSelectorComponent;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.JavaService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.service.PingService;
import ru.ricardocraft.client.utils.SystemMemory;
import ru.ricardocraft.client.utils.helper.JVMHelper;

import java.text.MessageFormat;

@Component
@Scope("prototype")
public class SettingsScene extends BaseSettingsScene implements SceneSupportUserBlock {

    private final static long MAX_JAVA_MEMORY_X64 = 32 * 1024;
    private final static long MAX_JAVA_MEMORY_X32 = 1536;
    private Label ramLabel;
    private Slider ramSlider;
    private RuntimeSettings.ProfileSettingsView profileSettings;
    private JavaSelectorComponent javaSelector;
    private UserBlock userBlock;

    private final GuiModuleConfig guiModuleConfig;
    private final SettingsManager settingsManager;
    private final JavaService javaService;
    private final SkinManager skinManager;
    private final PingService pingService;

    public SettingsScene(LauncherConfig config,
                         GuiModuleConfig guiModuleConfig,
                         SettingsManager settingsManager,
                         AuthService authService,
                         SkinManager skinManager,
                         LaunchService launchService,
                         JavaService javaService,
                         PingService pingService) {
        super("scenes/settings/settings.fxml", JavaFXApplication.getInstance(), config, guiModuleConfig, authService, launchService, settingsManager);
        this.guiModuleConfig = guiModuleConfig;
        this.settingsManager = settingsManager;
        this.javaService = javaService;
        this.skinManager = skinManager;
        this.pingService = pingService;
    }

    @Override
    protected void doInit() {
        super.doInit();
        this.userBlock = new UserBlock(layout, authService, skinManager, launchService, new SceneAccessor());

        ramSlider = LookupHelper.lookup(componentList, "#ramSlider");
        ramLabel = LookupHelper.lookup(componentList, "#ramLabel");
        long maxSystemMemory;
        try {
            SystemInfo systemInfo = new SystemInfo();
            maxSystemMemory = (systemInfo.getHardware().getMemory().getTotal() >> 20);
        } catch (Throwable ignored) {
            try {
                maxSystemMemory = (SystemMemory.getPhysicalMemorySize() >> 20);
            } catch (Throwable ignored1) {
                maxSystemMemory = 2048;
            }
        }
        ramSlider.setMax(Math.min(maxSystemMemory, getJavaMaxMemory()));

        ramSlider.setSnapToTicks(true);
        ramSlider.setShowTickMarks(true);
        ramSlider.setShowTickLabels(true);
        ramSlider.setMinorTickCount(1);
        ramSlider.setMajorTickUnit(1024);
        ramSlider.setBlockIncrement(1024);
        ramSlider.setLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Double object) {
                return "%.0fG".formatted(object / 1024);
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });
        LookupHelper.<ButtonBase>lookupIfPossible(layout, "#back").ifPresent(a -> a.setOnAction((e) -> {
            try {
                profileSettings = null;
                switchToBackScene();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        }));
        reset();
    }

    private long getJavaMaxMemory() {
        if (javaService.isArchAvailable(JVMHelper.ARCH.X86_64) || javaService.isArchAvailable(
                JVMHelper.ARCH.ARM64)) {
            return MAX_JAVA_MEMORY_X64;
        }
        return MAX_JAVA_MEMORY_X32;
    }

    @Override
    public void reset() {
        super.reset();
        profileSettings = new RuntimeSettings.ProfileSettingsView(settingsManager.getProfileSettings());
        javaSelector = new JavaSelectorComponent(javaService, componentList, profileSettings,
                settingsManager.getProfile());
        ramSlider.setValue(profileSettings.ram);
        ramSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            profileSettings.ram = newValue.intValue();
            updateRamLabel();
        });
        updateRamLabel();
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
                profileSettings.apply();
                settingsManager.process(profile, settingsManager.getOptionalView());
                switchToBackScene();
            } catch (Exception exception) {
                errorHandle(exception);
            }
        });
        serverButton.enableResetButton(null, (e) -> reset());
        add("Debug", settingsManager.getRuntimeSettings().globalSettings.debugAllClients
                        || profileSettings.debug, (value) -> profileSettings.debug = value,
                settingsManager.getRuntimeSettings().globalSettings.debugAllClients);
        add("AutoEnter", profileSettings.autoEnter, (value) -> profileSettings.autoEnter = value, false);
        add("Fullscreen", profileSettings.fullScreen, (value) -> profileSettings.fullScreen = value, false);
        if (JVMHelper.OS_TYPE == JVMHelper.OS.LINUX) {
            add("WaylandSupport", profileSettings.waylandSupport, (value) -> profileSettings.waylandSupport = value, false);
        }
        if (authService.checkDebugPermission("skipupdate")) {
            add("DebugSkipUpdate", profileSettings.debugSkipUpdate, (value) -> profileSettings.debugSkipUpdate = value, false);
        }
        if (authService.checkDebugPermission("skipfilemonitor")) {
            add("DebugSkipFileMonitor", profileSettings.debugSkipFileMonitor, (value) -> profileSettings.debugSkipFileMonitor = value, false);
        }
        userBlock.reset();
    }

    @Override
    public UserBlock getUserBlock() {
        return userBlock;
    }

    @Override
    public String getName() {
        return "settings";
    }

    public void updateRamLabel() {
        ramLabel.setText(profileSettings.ram == 0
                ? launchService.getTranslation("runtime.scenes.settings.ramAuto")
                : MessageFormat.format(launchService.getTranslation("runtime.scenes.settings.ram"),
                profileSettings.ram));
    }
}
