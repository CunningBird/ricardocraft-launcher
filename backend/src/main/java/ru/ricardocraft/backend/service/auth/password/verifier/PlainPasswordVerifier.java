package ru.ricardocraft.backend.service.auth.password.verifier;

public class PlainPasswordVerifier extends PasswordVerifier {
    @Override
    public boolean check(String encryptedPassword, String password) {
        return encryptedPassword.equals(password);
    }

    @Override
    public String encrypt(String password) {
        return super.encrypt(password);
    }
}
