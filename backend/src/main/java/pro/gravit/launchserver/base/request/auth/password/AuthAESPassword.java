package pro.gravit.launchserver.base.request.auth.password;

import pro.gravit.launchserver.base.request.auth.AuthRequest;
import pro.gravit.launchserver.core.LauncherNetworkAPI;

public class AuthAESPassword implements AuthRequest.AuthPasswordInterface {
    @LauncherNetworkAPI
    public final byte[] password;

    public AuthAESPassword(byte[] aesEncryptedPassword) {
        this.password = aesEncryptedPassword;
    }

    @Override
    public boolean check() {
        return true;
    }
}
