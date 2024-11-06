package ru.ricardocraft.bff.auth.password;

import org.bouncycastle.crypto.generators.OpenBSDBCrypt;
import ru.ricardocraft.bff.helper.SecurityHelper;

public class BCryptPasswordVerifier extends PasswordVerifier {
    public int cost = 10;

    @Override
    public boolean check(String encryptedPassword, String password) {
        return OpenBSDBCrypt.checkPassword(encryptedPassword, password.toCharArray());
    }

    @Override
    public String encrypt(String password) {
        return OpenBSDBCrypt.generate(password.toCharArray(), SecurityHelper.randomBytes(16), cost);
    }
}
