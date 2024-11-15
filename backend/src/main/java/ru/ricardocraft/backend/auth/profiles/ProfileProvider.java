package ru.ricardocraft.backend.auth.profiles;

import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.socket.Client;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class ProfileProvider {

    public abstract void sync() throws IOException;

    public abstract Set<ClientProfile> getProfiles();

    public abstract void addProfile(ClientProfile profile) throws IOException;

    public abstract void deleteProfile(ClientProfile profile) throws IOException;

    public ClientProfile getProfile(UUID uuid) {
        for (var e : getProfiles()) {
            if (e.getUUID().equals(uuid)) {
                return e;
            }
        }
        return null;
    }

    public ClientProfile getProfile(String title) {
        for (var e : getProfiles()) {
            if (e.getTitle().equals(title)) {
                return e;
            }
        }
        return null;
    }

    abstract public List<ClientProfile> getProfiles(Client client);

    abstract  public void syncProfilesDir() throws IOException;
}
