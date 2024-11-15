package ru.ricardocraft.backend.auth.profiles;

import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.handlers.NettyServerSocketHandler;
import ru.ricardocraft.backend.utils.ProviderMap;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class ProfileProvider {
    public static final ProviderMap<ProfileProvider> providers = new ProviderMap<>("ProfileProvider");
    private static boolean registredProviders = false;

    public static void registerProviders() {
        if (!registredProviders) {
            providers.register("local", LocalProfileProvider.class);
            registredProviders = true;
        }
    }

    public abstract void sync() throws IOException;

    public abstract Set<ClientProfile> getProfiles();

    public abstract void addProfile(ClientProfile profile) throws IOException;

    public abstract void deleteProfile(ClientProfile profile) throws IOException;

    public ClientProfile getProfile(UUID uuid) {
        for(var e : getProfiles()) {
            if(e.getUUID().equals(uuid)) {
                return e;
            }
        }
        return null;
    }

    public ClientProfile getProfile(String title) {
        for(var e : getProfiles()) {
            if(e.getTitle().equals(title)) {
                return e;
            }
        }
        return null;
    }

    abstract public List<ClientProfile> getProfiles(Client client);

    public void syncProfilesDir(LaunchServerConfig config, NettyServerSocketHandler nettyServerSocketHandler) throws IOException {
        this.sync();
        if (config.netty.sendProfileUpdatesEvent) {
            if (nettyServerSocketHandler == null || nettyServerSocketHandler.nettyServer == null) {
                return;
            }
            nettyServerSocketHandler.nettyServer.service.forEachActiveChannels((ch, handler) -> {
                Client client = handler.getClient();
                if (client == null || !client.isAuth) {
                    return;
                }
                ProfilesRequestEvent event = new ProfilesRequestEvent(this.getProfiles(client));
                event.requestUUID = RequestEvent.eventUUID;
                handler.service.sendObject(ch, event);
            });
        }
    }
}
