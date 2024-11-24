package ru.ricardocraft.client.base.modules.events;

import com.google.gson.GsonBuilder;
import ru.ricardocraft.client.base.modules.LauncherModule;

public class PreGsonPhase extends LauncherModule.Event {
    public final GsonBuilder gsonBuilder;

    public PreGsonPhase(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }
}
