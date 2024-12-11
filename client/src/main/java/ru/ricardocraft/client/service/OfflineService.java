package ru.ricardocraft.client.service;

import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.websockets.OfflineRequestService;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.RuntimeSettings;

public class OfflineService {
    private final RuntimeSettings runtimeSettings;
    private final GuiModuleConfig guiModuleConfig;

    public OfflineService(RuntimeSettings runtimeSettings, GuiModuleConfig guiModuleConfig) {
        this.runtimeSettings = runtimeSettings;
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
