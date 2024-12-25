package ru.ricardocraft.backend.socket;

import ru.ricardocraft.backend.auth.AuthProviderPair;
import ru.ricardocraft.backend.auth.core.interfaces.UserHardware;
import ru.ricardocraft.backend.base.ClientPermissions;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.dto.response.auth.AuthResponse;
import ru.ricardocraft.backend.repository.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Client {
    ReadWriteLock lock = new ReentrantReadWriteLock();
    public String auth_id;
    public long timestamp;
    public AuthResponse.ConnectTypes type;
    public ClientProfile profile;
    public boolean isAuth;
    public boolean checkSign;
    public ClientPermissions permissions;
    public String username;
    public UUID uuid;
    public TrustLevel trustLevel;

    public transient AuthProviderPair auth;

    public transient User coreObject;

    public transient ru.ricardocraft.backend.auth.core.UserSession sessionObject;

    public transient Map<String, Object> properties;

    public Map<String, Object> staticProperties;

    public Client() {
        timestamp = System.currentTimeMillis();
        type = null;
        isAuth = false;
        permissions = new ClientPermissions();
        username = "";
        checkSign = false;
    }

    //Данные авторизации
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        if (properties == null) properties = new HashMap<>();
        return (T) properties.get(name);
    }

    public <T> void setProperty(String name, T object) {
        if (properties == null) properties = new HashMap<>();
        properties.put(name, object);
    }

    public <T> void setStaticProperty(String name, T value) {
        if (staticProperties == null) staticProperties = new HashMap<>();
        staticProperties.put(name, value);
    }

    public User getUser() {
        if (coreObject != null) return coreObject;
        if (auth != null && uuid != null) {
            coreObject = auth.core.getUserByUUID(uuid);
        }
        return coreObject;
    }

    public Lock readLock() {
        return lock.readLock();
    }

    public Lock writeLock() {
        return lock.writeLock();
    }

    public static class TrustLevel {
        public byte[] verifySecureKey;
        public boolean keyChecked;
        public byte[] publicKey;
        public UserHardware hardwareInfo;
        // May be used later
        public double rating;
        public long latestMillis;
    }
}
