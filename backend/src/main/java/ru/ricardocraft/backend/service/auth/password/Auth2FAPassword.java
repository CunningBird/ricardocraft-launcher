package ru.ricardocraft.backend.service.auth.password;

import lombok.NoArgsConstructor;

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
