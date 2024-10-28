package pro.gravit.launchserver.base.request.auth.password;

import pro.gravit.launchserver.base.request.auth.AuthRequest;

public class AuthTOTPPassword implements AuthRequest.AuthPasswordInterface {
    public String totp;

    @Override
    public boolean check() {
        return true;
    }
}
