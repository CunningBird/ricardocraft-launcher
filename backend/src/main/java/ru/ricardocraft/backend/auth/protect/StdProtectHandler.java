package ru.ricardocraft.backend.auth.protect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.auth.AuthResponse;

import java.util.*;

public class StdProtectHandler extends ProtectHandler implements ProfilesProtectHandler {
    private transient final Logger logger = LogManager.getLogger();
    public Map<String, List<String>> profileWhitelist = new HashMap<>();
    public List<String> allowUpdates = new ArrayList<>();

    @Override
    public boolean allowGetAccessToken(AuthResponse.AuthContext context) {
        return (context.authType == AuthResponse.ConnectTypes.CLIENT) && context.client.checkSign;
    }

    @Override
    public void init(LaunchServerConfig config, KeyAgreementManager keyAgreementManager) {
        if (profileWhitelist != null && !profileWhitelist.isEmpty()) {
            logger.warn("profileWhitelist deprecated. Please use permission 'launchserver.profile.PROFILE_UUID.show' and 'launchserver.profile.PROFILE_UUID.enter'");
        }
    }

    @Override
    public boolean canGetProfile(ClientProfile profile, Client client) {
        return (client.isAuth && !profile.isLimited()) || isWhitelisted("launchserver.profile.%s.show", profile, client);
    }

    @Override
    public boolean canChangeProfile(ClientProfile profile, Client client) {
        return (client.isAuth && !profile.isLimited()) || isWhitelisted("launchserver.profile.%s.enter", profile, client);
    }

    @Override
    public boolean canGetUpdates(String updatesDirName, Client client) {
        return client.profile != null && (client.profile.getDir().equals(updatesDirName) || client.profile.getAssetDir().equals(updatesDirName) || allowUpdates.contains(updatesDirName));
    }

    private boolean isWhitelisted(String property, ClientProfile profile, Client client) {
        if (client.permissions != null) {
            String permByUUID = property.formatted(profile.getUUID());
            if (client.permissions.hasPerm(permByUUID)) {
                return true;
            }
            String permByTitle = property.formatted(profile.getTitle().toLowerCase(Locale.ROOT));
            if (client.permissions.hasPerm(permByTitle)) {
                return true;
            }
        }
        List<String> allowedUsername = profileWhitelist.get(profile.getTitle());
        return allowedUsername != null && allowedUsername.contains(client.username);
    }
}
