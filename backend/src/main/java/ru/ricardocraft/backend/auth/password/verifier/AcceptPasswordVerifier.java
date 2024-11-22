package ru.ricardocraft.backend.auth.password.verifier;

public class AcceptPasswordVerifier extends PasswordVerifier {
    @Override
    public boolean check(String encryptedPassword, String password) {
        return true;
    }

    @Override
    public String encrypt(String password) {
        return "";
    }
}
