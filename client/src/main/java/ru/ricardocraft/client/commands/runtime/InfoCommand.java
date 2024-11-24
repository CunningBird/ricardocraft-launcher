package ru.ricardocraft.client.commands.runtime;

import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.helper.JVMHelper;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends Command {
    private final JavaFXApplication application;

    public InfoCommand(JavaFXApplication application) {
        this.application = application;
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "show javafx info";
    }

    @Override
    public void invoke(String... args) {
        Platform.runLater(() -> {
            LogHelper.info("OS %s ARCH %s Java %d", JVMHelper.OS_TYPE.name(), JVMHelper.ARCH_TYPE.name(), JVMHelper.JVM_VERSION);
            {
                List<String> supportedFeatures = new ArrayList<>();
                List<String> unsupportedFeatures = new ArrayList<>();
                for (var e : ConditionalFeature.values()) {
                    if (Platform.isSupported(e)) {
                        supportedFeatures.add(e.name());
                    } else {
                        unsupportedFeatures.add(e.name());
                    }
                }
                LogHelper.info("JavaFX supported features: [%s]", String.join(",", supportedFeatures));
                LogHelper.info("JavaFX unsupported features: [%s]", String.join(",", unsupportedFeatures));
            }
            LogHelper.info("Is accessibility active %s", Platform.isAccessibilityActive() ? "true" : "false");
        });
    }
}