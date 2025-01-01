package ru.ricardocraft.client.core.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.config.RuntimeSettings;
import ru.ricardocraft.client.core.Launcher;
import ru.ricardocraft.client.core.hasher.HashedEntry;
import ru.ricardocraft.client.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.client.dto.request.websockets.StdWebSocketService;
import ru.ricardocraft.client.helper.CommonHelper;
import ru.ricardocraft.client.launch.RuntimeModuleManager;
import ru.ricardocraft.client.modules.events.PreGsonPhase;
import ru.ricardocraft.client.runtime.client.UserSettings;
import ru.ricardocraft.client.utils.ProviderMap;
import ru.ricardocraft.client.utils.UniversalJsonAdapter;

@Component
public class GsonManager {

    public final static String RUNTIME_NAME = "stdruntime";
    private final ProviderMap<UserSettings> providers = new ProviderMap<>();

    public Gson gson;
    public Gson configGson;

    private final RuntimeModuleManager moduleManager;

    @Autowired
    public GsonManager(RuntimeModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        providers.register(RUNTIME_NAME, RuntimeSettings.class);

        GsonBuilder gsonBuilder = CommonHelper.newBuilder();
        GsonBuilder configGsonBuilder = CommonHelper.newBuilder();
        configGsonBuilder.setPrettyPrinting().disableHtmlEscaping();
        registerAdapters(gsonBuilder);
        registerAdapters(configGsonBuilder);
        gson = gsonBuilder.create();
        configGson = configGsonBuilder.create();

        Launcher.gsonManager = this;
    }

    public void registerAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
        builder.registerTypeAdapter(UserSettings.class, new UniversalJsonAdapter<>(providers));
        StdWebSocketService.appendTypeAdapters(builder);
        moduleManager.invokeEvent(new PreGsonPhase(builder));
    }
}
