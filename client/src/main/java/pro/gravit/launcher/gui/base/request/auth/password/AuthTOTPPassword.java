package pro.gravit.launcher.gui.base.request.auth.password;

import pro.gravit.launcher.gui.base.request.auth.AuthRequest;

public class AuthTOTPPassword implements AuthRequest.AuthPasswordInterface {
    public String totp;

    @Override
    public boolean check() {
        return true;
    }
}
