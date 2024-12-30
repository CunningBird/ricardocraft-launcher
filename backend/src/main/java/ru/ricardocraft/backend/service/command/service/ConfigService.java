package ru.ricardocraft.backend.service.command.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.properties.config.ProguardConfig;
import ru.ricardocraft.backend.repository.User;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.auth.AuthLimiter;
import ru.ricardocraft.backend.service.auth.core.AuthCoreProvider;
import ru.ricardocraft.backend.service.auth.password.AuthPassword;
import ru.ricardocraft.backend.service.auth.password.AuthPlainPassword;

import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConfigService {

    private final AuthLimiter authLimiter;
    private final AuthCoreProvider authCoreProvider;
    private final ObjectMapper objectMapper;
    private final ProguardConfig proguardConfig;
    private final DirectoriesService directoriesService;

    public void configAuthLimiterClear() {
        long size = authLimiter.getMap().size();
        authLimiter.getMap().clear();
        log.info("Cleared {} entity", size);
    }

    public void configAuthLimiterAddExclude(String exclusion) {
        authLimiter.exclude.add(authLimiter.getFromString(exclusion));
    }

    public void configAuthLimiterClearExclude() {
        authLimiter.exclude.clear();
    }

    public void configAuthLimiterGc() {
        long size = authLimiter.getMap().size();
        authLimiter.garbageCollection();
        log.info("Cleared {} entity", size);
    }

    public void configAuthLimiterRmExclude(String exclude) {
        authLimiter.exclude.remove(authLimiter.getFromString(exclude));
    }

    public void configAuthProviderAuth(String login, @Nullable String passwordData) throws Exception {
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

    public void configAuthProviderGetUserByUsername(String username) {
        User user = authCoreProvider.getUserByUsername(username);
        if (user == null) {
            log.info("User {} not found", username);
        } else {
            log.info("User {}: {}", username, user);
        }
    }

    public void configAuthProviderGetUserByUuid(String uuid) {
        User user = authCoreProvider.getUserByUUID(UUID.fromString(uuid));
        if (user == null) {
            log.info("User {} not found", uuid);
        } else {
            log.info("User {}: {}", uuid, user);
        }
    }

    public void configProGuardClean() throws Exception {
        proguardConfig.prepare(true);
        Files.deleteIfExists(directoriesService.getProguardMappingsFile());
    }

    public void configProGuardRegen() throws Exception {
        proguardConfig.genWords(true);
    }

    public void configProGuardReset() throws Exception {
        proguardConfig.prepare(true);
        Files.deleteIfExists(directoriesService.getProguardMappingsFile());
    }
}
