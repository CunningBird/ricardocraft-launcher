package ru.ricardocraft.client.service;

import org.springframework.stereotype.Component;
import ru.ricardocraft.client.base.Launcher;
import ru.ricardocraft.client.base.events.request.AuthRequestEvent;
import ru.ricardocraft.client.base.events.request.GetAvailabilityAuthRequestEvent;
import ru.ricardocraft.client.base.profiles.PlayerProfile;
import ru.ricardocraft.client.base.request.Request;
import ru.ricardocraft.client.base.request.auth.AuthRequest;
import ru.ricardocraft.client.base.request.auth.password.AuthAESPassword;
import ru.ricardocraft.client.base.request.auth.password.AuthPlainPassword;
import ru.ricardocraft.client.config.GuiModuleConfig;
import ru.ricardocraft.client.config.LauncherConfig;
import ru.ricardocraft.client.utils.helper.SecurityHelper;

@Component
public class AuthService {
    private final LauncherConfig config = Launcher.getConfig();

    private final GuiModuleConfig guiModuleConfig;

    private AuthRequestEvent rawAuthResult;
    private GetAvailabilityAuthRequestEvent.AuthAvailability authAvailability;

    public AuthService(GuiModuleConfig guiModuleConfig) {
        this.guiModuleConfig = guiModuleConfig;
    }

    public AuthRequest.AuthPasswordInterface makePassword(String plainPassword) {
        if (config.passwordEncryptKey != null) {
            try {
                return new AuthAESPassword(encryptAESPassword(plainPassword));
            } catch (Exception ignored) {
            }
        }
        return new AuthPlainPassword(plainPassword);
    }

    public AuthRequest makeAuthRequest(String login, AuthRequest.AuthPasswordInterface password, String authId) {
        // TODO apply checkSign to client
//        return new AuthRequest(login, password, authId, false, application.isDebugMode()
//                ? AuthRequest.ConnectTypes.API
//                : AuthRequest.ConnectTypes.CLIENT);
        return new AuthRequest(login, password, authId, false, AuthRequest.ConnectTypes.API);
    }

    private byte[] encryptAESPassword(String password) throws Exception {
        return SecurityHelper.encrypt(Launcher.getConfig().passwordEncryptKey, password);
    }

    public void setAuthResult(String authId, AuthRequestEvent rawAuthResult) {
        this.rawAuthResult = rawAuthResult;
        if (rawAuthResult.oauth != null) {
            Request.setOAuth(authId, rawAuthResult.oauth);
        }
    }

    public void setAuthAvailability(GetAvailabilityAuthRequestEvent.AuthAvailability info) {
        this.authAvailability = info;
    }

    public GetAvailabilityAuthRequestEvent.AuthAvailability getAuthAvailability() {
        return authAvailability;
    }

    public boolean isFeatureAvailable(String name) {
        return authAvailability.features != null && authAvailability.features.contains(name);
    }

    public String getUsername() {
        if (rawAuthResult == null || rawAuthResult.playerProfile == null) return "Player";
        return rawAuthResult.playerProfile.username;
    }

    public String getMainRole() {
        if (rawAuthResult == null
                || rawAuthResult.permissions == null
                || rawAuthResult.permissions.getRoles() == null
                || rawAuthResult.permissions.getRoles().isEmpty()) return "";
        return rawAuthResult.permissions.getRoles().get(0);
    }

    public boolean checkPermission(String name) {
        if (rawAuthResult == null || rawAuthResult.permissions == null) {
            return false;
        }
        return rawAuthResult.permissions.hasPerm(name);
    }

    public boolean checkDebugPermission(String name) {
        return (!guiModuleConfig.disableDebugPermissions && checkPermission("launcher.debug." + name));
    }

    public PlayerProfile getPlayerProfile() {
        if (rawAuthResult == null) return null;
        return rawAuthResult.playerProfile;
    }

    public String getAccessToken() {
        if (rawAuthResult == null) return null;
        return rawAuthResult.accessToken;
    }

    public void exit() {
        rawAuthResult = null;
        //.profile = null;
    }
}
