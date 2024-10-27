package pro.gravit.launcher.gui.start;

import pro.gravit.launcher.gui.start.ClientLauncherWrapper;

public interface ClientWrapperModule {
    void wrapperPhase(ClientLauncherWrapper.ClientLauncherWrapperContext context);
}
