package pro.gravit.launcher.gui.client.events;

import pro.gravit.launcher.gui.base.modules.LauncherModule;
import pro.gravit.launcher.gui.base.profiles.ClientProfile;
import pro.gravit.launcher.gui.utils.launch.ClassLoaderControl;
import pro.gravit.launcher.gui.utils.launch.Launch;

public class ClientProcessClassLoaderEvent extends LauncherModule.Event {
    public final Launch launch;
    public final ClassLoaderControl classLoaderControl;
    public final ClientProfile profile;

    public ClientProcessClassLoaderEvent(Launch launch, ClassLoaderControl classLoaderControl, ClientProfile profile) {
        this.launch = launch;
        this.classLoaderControl = classLoaderControl;
        this.profile = profile;
    }
}
