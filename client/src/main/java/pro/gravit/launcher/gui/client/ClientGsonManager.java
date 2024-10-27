package pro.gravit.launcher.gui.client;

import com.google.gson.GsonBuilder;
import pro.gravit.launcher.gui.base.modules.events.PreGsonPhase;
import pro.gravit.launcher.gui.base.request.websockets.ClientWebSocketService;
import pro.gravit.launcher.gui.client.ClientModuleManager;
import pro.gravit.launcher.core.managers.GsonManager;

public class ClientGsonManager extends GsonManager {
    private final pro.gravit.launcher.gui.client.ClientModuleManager moduleManager;

    public ClientGsonManager(ClientModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }

    @Override
    public void registerAdapters(GsonBuilder builder) {
        super.registerAdapters(builder);
        ClientWebSocketService.appendTypeAdapters(builder);
        moduleManager.invokeEvent(new PreGsonPhase(builder));
    }
}
