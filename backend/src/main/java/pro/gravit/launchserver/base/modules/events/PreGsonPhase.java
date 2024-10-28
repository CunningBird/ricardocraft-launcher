package pro.gravit.launchserver.base.modules.events;

import com.google.gson.GsonBuilder;
import pro.gravit.launchserver.base.modules.LauncherModule;

public class PreGsonPhase extends LauncherModule.Event {
    public final GsonBuilder gsonBuilder;

    public PreGsonPhase(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }
}
