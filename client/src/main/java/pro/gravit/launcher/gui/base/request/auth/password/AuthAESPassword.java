package pro.gravit.launcher.gui.base.request.auth.password;

import pro.gravit.launcher.gui.core.LauncherNetworkAPI;
import pro.gravit.launcher.gui.base.request.auth.AuthRequest;

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
