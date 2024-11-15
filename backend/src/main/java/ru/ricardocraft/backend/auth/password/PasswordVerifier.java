package ru.ricardocraft.backend.auth.password;

public abstract class PasswordVerifier {

    public abstract boolean check(String encryptedPassword, String password);

    public String encrypt(String password) {
        throw new UnsupportedOperationException();
    }
}
