package pro.gravit.launcher.gui.runtime.client;

import com.google.gson.GsonBuilder;
import pro.gravit.launcher.gui.base.modules.events.PreGsonPhase;
import pro.gravit.launcher.gui.base.request.websockets.ClientWebSocketService;
import pro.gravit.launcher.gui.core.managers.GsonManager;
import pro.gravit.launcher.gui.start.RuntimeModuleManager;
import pro.gravit.launcher.gui.utils.UniversalJsonAdapter;

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
