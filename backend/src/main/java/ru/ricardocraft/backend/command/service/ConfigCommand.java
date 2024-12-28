package ru.ricardocraft.backend.command.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.auth.AuthLimiter;
import ru.ricardocraft.backend.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.auth.password.AuthPassword;
import ru.ricardocraft.backend.auth.password.AuthPlainPassword;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.properties.config.ProguardConfig;
import ru.ricardocraft.backend.repository.User;

import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@ShellComponent
@ShellCommandGroup("service")
@RequiredArgsConstructor
public class ConfigCommand {

    private final AuthLimiter authLimiter;
    private final AuthCoreProvider authCoreProvider;
    private final ObjectMapper objectMapper;
    private final ProguardConfig proguardConfig;
    private final DirectoriesManager directoriesManager;

    @ShellMethod("[] Clear authLimiter map")
    public void configAuthLimiterClear() {
        long size = authLimiter.getMap().size();
        authLimiter.getMap().clear();
        log.info("Cleared {} entity", size);
    }

    @ShellMethod("[exclusion] Add exclusion to authLimiter")
    public void configAuthLimiterAddExclude(@ShellOption String exclusion) {
        authLimiter.exclude.add(authLimiter.getFromString(exclusion));
    }

    @ShellMethod("[] Clear exclusions in authLimiter")
    public void configAuthLimiterClearExclude() {
        authLimiter.exclude.clear();
    }

    @ShellMethod("[] invoke GC for authLimiter")
    public void configAuthLimiterGc() {
        long size = authLimiter.getMap().size();
        authLimiter.garbageCollection();
        log.info("Cleared {} entity", size);
    }

    @ShellMethod("[] Remove exclusion from authLimiter")
    public void configAuthLimiterRmExclude(@ShellOption String exclude) {
        authLimiter.exclude.remove(authLimiter.getFromString(exclude));
    }

    @ShellMethod("[login] (json/plain password data) Test auth")
    public void configAuthProviderAuth(@ShellOption String login,
                                       @ShellOption(defaultValue = ShellOption.NULL) String passwordData) throws Exception {
        AuthPassword password = null;
        if (passwordData != null) {
            if (passwordData.startsWith("{")) {
                password = objectMapper.readValue(passwordData, AuthPassword.class);
            } else {
                password = new AuthPlainPassword(passwordData);
            }
        }
        var report = authCoreProvider.authorize(login, null, password, false);
        if (report.isUsingOAuth()) {
            log.info("OAuth: AccessToken: {} RefreshToken: {} MinecraftAccessToken: {}", report.oauthAccessToken(), report.oauthRefreshToken(), report.minecraftAccessToken());
            if (report.session() != null) {
                log.info("UserSession: id {} expire {} user {}", report.session().getID(), report.session().getExpireIn(), report.session().getUser() == null ? "null" : "found");
                log.info(report.session().toString());
            }
        } else {
            log.info("Basic: MinecraftAccessToken: {}", report.minecraftAccessToken());
        }
    }

    @ShellMethod("[username] get user by username")
    public void configAuthProviderGetUserByUsername(@ShellOption String username) {
        User user = authCoreProvider.getUserByUsername(username);
        if (user == null) {
            log.info("User {} not found", username);
        } else {
            log.info("User {}: {}", username, user.toString());
        }
    }

    @ShellMethod("[uuid] get user by uuid")
    public void configAuthProviderGetUserByUuid(@ShellOption String uuid) {
        User user = authCoreProvider.getUserByUUID(UUID.fromString(uuid));
        if (user == null) {
            log.info("User {} not found", uuid);
        } else {
            log.info("User {}: {}", uuid, user.toString());
        }
    }

    @ShellMethod("[] reset proguard config")
    public void configProGuardClean() throws Exception {
        proguardConfig.prepare(true);
        Files.deleteIfExists(directoriesManager.getProguardMappingsFile());
    }

    @ShellMethod("[] regenerate proguard dictionary")
    public void configProGuardRegen() throws Exception {
        proguardConfig.genWords(true);
    }

    @ShellMethod("[] reset proguard config")
    public void configProGuardReset() throws Exception {
        proguardConfig.prepare(true);
        Files.deleteIfExists(directoriesManager.getProguardMappingsFile());
    }
}
