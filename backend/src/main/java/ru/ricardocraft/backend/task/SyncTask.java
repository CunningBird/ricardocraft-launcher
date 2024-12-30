package ru.ricardocraft.backend.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;
import ru.ricardocraft.backend.service.auth.updates.UpdatesProvider;

import java.io.IOException;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "launch-server-config.sign.checkCertificateExpired")
@RequiredArgsConstructor
public class SyncTask {

    private final ProfileProvider profileProvider;

    private final UpdatesProvider updatesProvider;

    @Scheduled(fixedDelayString = "P1D") // TODO configure interval
    public void syncProfilesAndUpdates() {
        try {
            // Sync profiles dir
            profileProvider.syncProfilesDir();
            // Sync updates dir
            updatesProvider.syncInitially();
        } catch (IOException e) {
            log.error("Updates/Profiles not synced", e);
        }
    }
}
