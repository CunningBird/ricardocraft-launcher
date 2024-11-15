package ru.ricardocraft.backend.core.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.ricardocraft.backend.core.hasher.HashedEntry;
import ru.ricardocraft.backend.core.hasher.HashedEntryAdapter;
import ru.ricardocraft.backend.helper.CommonHelper;

public class GsonManager {
    public GsonBuilder gsonBuilder;
    public Gson gson;
    public GsonBuilder configGsonBuilder;
    public Gson configGson;

    public void initGson() {
        gsonBuilder = CommonHelper.newBuilder();
        configGsonBuilder = CommonHelper.newBuilder();
        configGsonBuilder.setPrettyPrinting().disableHtmlEscaping();
        registerAdapters(gsonBuilder);
        registerAdapters(configGsonBuilder);
        gson = gsonBuilder.create();
        configGson = configGsonBuilder.create();
    }

    public void registerAdapters(GsonBuilder builder) {
        builder.registerTypeAdapter(HashedEntry.class, new HashedEntryAdapter());
    }
}
