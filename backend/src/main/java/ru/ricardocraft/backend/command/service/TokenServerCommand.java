package ru.ricardocraft.backend.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.auth.AuthProviderPair;
import ru.ricardocraft.backend.service.auth.AuthProviders;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;
import ru.ricardocraft.backend.service.AuthService;
import ru.ricardocraft.backend.service.profiles.ClientProfile;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class TokenServerCommand {

    private final AuthService authService;
    private final ProfileProvider profileProvider;
    private final AuthProviders authProviders;

    @ShellMethod("[profileName] (authId) (public only) generate new server token")
    public void tokenServer(@ShellOption String profileName,
                            @ShellOption(defaultValue = ShellOption.NULL) String authId,
                            @ShellOption(defaultValue = "false") Boolean publicOnly) {
        AuthProviderPair pair = (authId != null) ? authProviders.getAuthProviderPair(authId) : authProviders.getAuthProviderPair();
        ClientProfile profile = null;
        for (ClientProfile p : profileProvider.getProfiles()) {
            if (p.getTitle().equals(profileName) || p.getUUID().toString().equals(profileName)) {
                profile = p;
                break;
            }
        }
        if (profile == null) {
            log.warn("Profile {} not found", profileName);
        }
        if (pair == null) {
            log.error("AuthId {} not found", authId);
            return;
        }
        String token = authService.newCheckServerToken(profile != null ? profile.getUUID().toString() : profileName, pair.name, publicOnly);
        log.info("Server token {} authId {}: {}", profileName, pair.name, token);
    }
}
