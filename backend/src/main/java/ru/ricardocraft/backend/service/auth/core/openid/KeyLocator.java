package ru.ricardocraft.backend.service.auth.core.openid;

import io.jsonwebtoken.Header;
import io.jsonwebtoken.JweHeader;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.LocatorAdapter;
import io.jsonwebtoken.security.Jwk;
import io.jsonwebtoken.security.JwkSet;

import java.security.Key;
import java.util.Map;
import java.util.stream.Collectors;

public class KeyLocator extends LocatorAdapter<Key> {
    private final Map<String, Key> keys;

    public KeyLocator(JwkSet jwks) {
        this.keys = jwks.getKeys().stream().collect(
                Collectors.toMap(jwk -> String.valueOf(jwk.get("kid")), Jwk::toKey));
    }

    @Override
    protected Key locate(JweHeader header) {
        return super.locate(header);
    }

    @Override
    protected Key locate(JwsHeader header) {
        return keys.get(header.getKeyId());
    }

    @Override
    protected Key doLocate(Header header) {
        return super.doLocate(header);
    }
}