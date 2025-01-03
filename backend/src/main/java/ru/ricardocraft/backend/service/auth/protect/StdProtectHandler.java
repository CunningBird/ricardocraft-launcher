package ru.ricardocraft.backend.service.auth.protect;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.service.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.dto.request.auth.AuthRequest;
import ru.ricardocraft.backend.service.controller.auth.AuthRequestService;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.controller.auth.AuthController;
import ru.ricardocraft.backend.controller.Client;

import java.util.List;
import java.util.Locale;

@Component
@Primary
public class StdProtectHandler extends ProtectHandler implements ProfilesProtectHandler {

    protected final LaunchServerProperties config;

    @Autowired
    public StdProtectHandler(LaunchServerProperties config) {
        this.config = config;
    }

    @Override
    public boolean allowGetAccessToken(AuthRequestService.AuthContext context) {
        return (context.authType == AuthRequest.ConnectTypes.CLIENT) && context.client.checkSign;
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
        return client.profile != null && (client.profile.getDir().equals(updatesDirName) || client.profile.getAssetDir().equals(updatesDirName) || config.getProtectHandler().getAllowUpdates().contains(updatesDirName));
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
        List<String> allowedUsername = config.getProtectHandler().getProfileWhitelist().get(profile.getTitle());
        return allowedUsername != null && allowedUsername.contains(client.username);
    }
}
