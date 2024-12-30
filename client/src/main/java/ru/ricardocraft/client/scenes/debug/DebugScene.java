package ru.ricardocraft.client.scenes.debug;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.helper.LookupHelper;
import ru.ricardocraft.client.runtime.managers.SettingsManager;
import ru.ricardocraft.client.scenes.AbstractScene;
import ru.ricardocraft.client.service.AuthService;
import ru.ricardocraft.client.service.LaunchService;
import ru.ricardocraft.client.utils.Version;
import ru.ricardocraft.client.helper.JVMHelper;
import ru.ricardocraft.client.helper.LogHelper;

import java.io.EOFException;

@Component
@Scope("prototype")
public class DebugScene extends AbstractScene {
    private ProcessLogOutput processLogOutput;
    private LaunchService.ClientInstance clientInstance;

    public DebugScene(LauncherConfig config,
                      GuiModuleConfig guiModuleConfig,
                      AuthService authService,
                      LaunchService launchService,
                      SettingsManager settingsManager) {
        super("scenes/debug/debug.fxml", JavaFXApplication.getInstance(), config, guiModuleConfig, authService, launchService, settingsManager);
        this.isResetOnShow = true;
    }

    @Override
    protected void doInit() {
        processLogOutput = new ProcessLogOutput(LookupHelper.lookup(layout, "#output"));
        LookupHelper.<ButtonBase>lookupIfPossible(header, "#controls", "#kill").ifPresent((x) -> x.setOnAction((e) -> {
            if (clientInstance != null) clientInstance.kill();
        }));

        LookupHelper.<Label>lookupIfPossible(layout, "#version").ifPresent((v) -> v.setText(getMiniLauncherInfo()));
        LookupHelper.<ButtonBase>lookupIfPossible(header, "#controls", "#copy").ifPresent((x) -> x.setOnAction((e) -> processLogOutput.copyToClipboard()));
        LookupHelper.<ButtonBase>lookup(header, "#back").setOnAction((e) -> {
            if (clientInstance != null) {
                clientInstance.unregisterListener(processLogOutput);
            }
            try {
                switchToBackScene();
            } catch (Exception ex) {
                errorHandle(ex);
            }
        });
    }


    @Override
    public void reset() {
        processLogOutput.clear();
    }

    public void onClientInstance(LaunchService.ClientInstance clientInstance) {
        this.clientInstance = clientInstance;
        this.clientInstance.registerListener(processLogOutput);
        this.clientInstance.getOnWriteParamsFuture().thenAccept((ok) -> processLogOutput.append("[START] Write param successful\n")).exceptionally((e) -> {
            errorHandle(e);
            return null;
        });
        this.clientInstance.start().thenAccept((code) -> processLogOutput.append(String.format("[START] Process exit with code %d", code))).exceptionally((e) -> {
            errorHandle(e);
            return null;
        });
    }

    public void append(String text) {
        processLogOutput.append(text);
    }

    @Override
    public void errorHandle(Throwable e) {
        if (!(e instanceof EOFException)) {
            if (LogHelper.isDebugEnabled()) processLogOutput.append(e.toString());
        }
    }

    @Override
    public String getName() {
        return "debug";
    }

    private String getMiniLauncherInfo() {
        return "Launcher %s | Java %d(%s) x%d | %s x%d"
                .formatted(Version.getVersion().toString(), JVMHelper.JVM_VERSION, System.getProperty("java.version"),
                        JVMHelper.JVM_BITS, JVMHelper.OS_TYPE.name(), JVMHelper.OS_BITS);
    }
}
