package pro.gravit.launcher.gui.base.request.auth.password;

import pro.gravit.launcher.gui.base.request.auth.AuthRequest;
import pro.gravit.launcher.core.LauncherNetworkAPI;

public class AuthPlainPassword implements AuthRequest.AuthPasswordInterface {
    @LauncherNetworkAPI
    public final String password;

    public AuthPlainPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean check() {
        return true;
    }
}
