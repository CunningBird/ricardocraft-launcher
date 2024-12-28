package ru.ricardocraft.backend.auth;

import ru.ricardocraft.backend.dto.response.auth.AuthResponse;

import java.io.IOException;
import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

public final class AuthException extends IOException {
    @Serial
    private static final long serialVersionUID = -2586107832847245863L;


    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public static AuthException need2FA() {
        return new AuthException(AuthResponse.TWO_FACTOR_NEED_ERROR_MESSAGE);
    }

    public static AuthException needMFA(List<Integer> factors) {
        String message = AuthResponse.ONE_FACTOR_NEED_ERROR_MESSAGE_PREFIX
                .concat(factors.stream().map(String::valueOf).collect(Collectors.joining(".")));
        return new AuthException(message);
    }

    public static AuthException wrongPassword() {
        return new AuthException(AuthResponse.WRONG_PASSWORD_ERROR_MESSAGE);
    }

    public static AuthException userNotFound() {
        return new AuthException(AuthResponse.USER_NOT_FOUND_ERROR_MESSAGE);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
