package ru.ricardocraft.client.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.websockets.OfflineRequestService;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.runtime.managers.SettingsManager;

@Component
public class OfflineService {
    private final RuntimeSettings runtimeSettings;
    private final GuiModuleConfig guiModuleConfig;

    @Autowired
    public OfflineService(SettingsManager settingsManager, GuiModuleConfig guiModuleConfig) {
        this.runtimeSettings = settingsManager.getRuntimeSettings();
        this.guiModuleConfig = guiModuleConfig;
    }

    public boolean isAvailableOfflineMode() {
        if (guiModuleConfig.disableOfflineMode) return false;
        return runtimeSettings.profiles != null;
    }

    public boolean isOfflineMode() {
        return Request.getRequestService() instanceof OfflineRequestService;
    }
}
