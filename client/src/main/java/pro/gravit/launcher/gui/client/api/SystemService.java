package pro.gravit.launcher.gui.client.api;

import pro.gravit.launcher.gui.client.ClientLauncherMethods;

public class SystemService {
    private SystemService() {
        throw new UnsupportedOperationException();
    }

    public static void exit(int code) {
        ClientLauncherMethods.exitLauncher(code);
    }
}
