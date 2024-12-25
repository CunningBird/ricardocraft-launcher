package ru.ricardocraft.backend.auth.protect.interfaces;

import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.socket.Client;

public interface ProfilesProtectHandler {
    default boolean canGetProfiles(Client client) {
        return true;
    }

    default boolean canGetProfile(ClientProfile profile, Client client) {
        return true;
    }

    default boolean canChangeProfile(ClientProfile profile, Client client) {
        return client.isAuth;
    }

    default boolean canGetUpdates(String updatesDirName, Client client) {
        return true;
    }
}
