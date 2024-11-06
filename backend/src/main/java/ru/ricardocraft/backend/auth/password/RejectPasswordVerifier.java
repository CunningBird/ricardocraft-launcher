package ru.ricardocraft.backend.auth.password;

public class RejectPasswordVerifier extends PasswordVerifier {
    @Override
    public boolean check(String encryptedPassword, String password) {
        return false;
    }
}
