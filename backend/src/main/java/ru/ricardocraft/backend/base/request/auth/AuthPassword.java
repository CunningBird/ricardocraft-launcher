package ru.ricardocraft.backend.base.request.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ru.ricardocraft.backend.base.request.auth.password.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthPlainPassword.class, name = "plain"),
        @JsonSubTypes.Type(value = AuthRSAPassword.class, name = "rsa2"),
        @JsonSubTypes.Type(value = AuthAESPassword.class, name = "aes"),
        @JsonSubTypes.Type(value = Auth2FAPassword.class, name = "2fa"),
        @JsonSubTypes.Type(value = AuthMultiPassword.class, name = "multi"),
        @JsonSubTypes.Type(value = AuthSignaturePassword.class, name = "signature"),
        @JsonSubTypes.Type(value = AuthTOTPPassword.class, name = "totp"),
        @JsonSubTypes.Type(value = AuthOAuthPassword.class, name = "oauth"),
        @JsonSubTypes.Type(value = AuthCodePassword.class, name = "code")
})
public abstract class AuthPassword {

    abstract public boolean check();

    public boolean isAllowSave() {
        return false;
    }
}
