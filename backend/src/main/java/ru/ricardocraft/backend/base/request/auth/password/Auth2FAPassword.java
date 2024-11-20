package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

@NoArgsConstructor
public class Auth2FAPassword extends AuthPassword {

    public AuthPassword firstPassword;
    public AuthPassword secondPassword;

    @Override
    public boolean check() {
        return firstPassword != null && firstPassword.check() && secondPassword != null && secondPassword.check();
    }

    @Override
    public boolean isAllowSave() {
        return firstPassword.isAllowSave() && secondPassword.isAllowSave();
    }
}
