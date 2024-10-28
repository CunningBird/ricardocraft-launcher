package pro.gravit.launchserver.base.request.auth.password;

import pro.gravit.launchserver.base.request.auth.AuthRequest;
import pro.gravit.launchserver.core.LauncherNetworkAPI;

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
