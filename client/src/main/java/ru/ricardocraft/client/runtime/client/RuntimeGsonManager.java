package ru.ricardocraft.client.runtime.client;

import com.google.gson.GsonBuilder;
import ru.ricardocraft.client.base.modules.events.PreGsonPhase;
import ru.ricardocraft.client.base.request.websockets.ClientWebSocketService;
import ru.ricardocraft.client.core.managers.GsonManager;
import ru.ricardocraft.client.launch.RuntimeModuleManager;
import ru.ricardocraft.client.utils.UniversalJsonAdapter;

public class RuntimeGsonManager extends GsonManager {
    private final RuntimeModuleManager moduleManager;

    public RuntimeGsonManager(RuntimeModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public void registerAdapters(GsonBuilder builder) {
        super.registerAdapters(builder);
        builder.registerTypeAdapter(UserSettings.class, new UniversalJsonAdapter<>(UserSettings.providers));
        ClientWebSocketService.appendTypeAdapters(builder);
        moduleManager.invokeEvent(new PreGsonPhase(builder));
    }
}
