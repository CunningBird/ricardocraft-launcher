package ru.ricardocraft.backend.base.request.auth.password;

import lombok.NoArgsConstructor;
import ru.ricardocraft.backend.base.request.auth.AuthPassword;

import java.util.List;

@NoArgsConstructor
public class AuthMultiPassword extends AuthPassword {

    public List<AuthPassword> list;

    @Override
    public boolean check() {
        return list != null && list.stream().allMatch(l -> l != null && l.check());
    }

    @Override
    public boolean isAllowSave() {
        return list != null && list.stream().allMatch(l -> l != null && l.isAllowSave());
    }
}
