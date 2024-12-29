package ru.ricardocraft.backend.service.auth.password.verifier;

public abstract class PasswordVerifier {

    public abstract boolean check(String encryptedPassword, String password);

    public String encrypt(String password) {
        throw new UnsupportedOperationException();
    }
}
