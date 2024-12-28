package ru.ricardocraft.backend.auth.profiles;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.service.ClientsCommand;
import ru.ricardocraft.backend.dto.AbstractResponse;
import ru.ricardocraft.backend.dto.response.auth.ProfilesResponse;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.properties.HttpServerProperties;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.ServerWebSocketHandler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Slf4j
@Component
public class LocalProfileProvider extends ProfileProvider {

    private final ClientsCommand clientsCommand;
    private transient volatile Map<Path, ClientProfile> profilesMap;
    private transient volatile Set<ClientProfile> profilesList; // Cache

    private final transient DirectoriesManager directoriesManager;
    private final transient HttpServerProperties httpServerProperties;
    private final transient ProtectHandler handler;
    private final transient ServerWebSocketHandler webSocketHandler;
    private final transient JacksonManager jacksonManager;

    @Autowired
    public LocalProfileProvider(DirectoriesManager directoriesManager,
                                HttpServerProperties httpServerProperties,
                                ProtectHandler handler,
                                ServerWebSocketHandler webSocketHandler,
                                JacksonManager jacksonManager, ClientsCommand clientsCommand) {
        this.directoriesManager = directoriesManager;
        this.httpServerProperties = httpServerProperties;
        this.handler = handler;
        this.webSocketHandler = webSocketHandler;
        this.jacksonManager = jacksonManager;
        this.clientsCommand = clientsCommand;
    }

    @Override
    public void sync() throws IOException {
        Map<Path, ClientProfile> newProfiles = new HashMap<>();
        IOHelper.walk(directoriesManager.getProfilesDir(), new ProfilesFileVisitor(newProfiles), false);
        Set<ClientProfile> newProfilesList = new HashSet<>(newProfiles.values());
        profilesMap = newProfiles;
        profilesList = newProfilesList;
    }

    @Override
    public Set<ClientProfile> getProfiles() {
        return profilesList;
    }

    @Override
    public void addProfile(ClientProfile profile) throws IOException {
        ClientProfile oldProfile;
        Path target = null;
        for (var e : profilesMap.entrySet()) {
            if (e.getValue().getUUID().equals(profile.getUUID())) {
                target = e.getKey();
            }
        }
        if (target == null) {
            target = directoriesManager.getProfilesDir().resolve(profile.getTitle() + ".json");
            oldProfile = profilesMap.get(target);
            if (oldProfile != null && !oldProfile.getUUID().equals(profile.getUUID())) {
                throw new FileAlreadyExistsException(target.toString());
            }
        }
        try (BufferedWriter writer = IOHelper.newWriter(target)) {
            jacksonManager.getMapper().writeValue(writer, profile);
        }
        addProfile(target, profile);
    }

    @Override
    public void deleteProfile(ClientProfile profile) throws IOException {
        for (var e : profilesMap.entrySet()) {
            if (e.getValue().getUUID().equals(profile.getUUID())) {
                Files.deleteIfExists(e.getKey());
                profilesMap.remove(e.getKey());
                profilesList.remove(e.getValue());
                break;
            }
        }
    }

    private void addProfile(Path path, ClientProfile profile) {
        for (var e : profilesMap.entrySet()) {
            if (e.getValue().getUUID().equals(profile.getUUID())) {
                profilesMap.remove(e.getKey());
                profilesList.remove(e.getValue());
                break;
            }
        }
        profilesMap.put(path, profile);
        profilesList.add(profile);
    }

    @Override
    public List<ClientProfile> getProfiles(Client client) {
        List<ClientProfile> profileList;
        Set<ClientProfile> serverProfiles = getProfiles();
        if (this.handler instanceof ProfilesProtectHandler protectHandler) {
            profileList = new ArrayList<>(4);
            for (ClientProfile profile : serverProfiles) {
                if (protectHandler.canGetProfile(profile, client)) {
                    profileList.add(profile);
                }
            }
        } else {
            profileList = List.copyOf(serverProfiles);
        }
        return profileList;
    }

    @Override
    public void syncProfilesDir() throws IOException {
        sync();
        if (httpServerProperties.getSendProfileUpdatesEvent()) {
            webSocketHandler.forEachActiveChannels((session, client) -> {
                if (client == null || !client.isAuth) {
                    return;
                }
                ProfilesResponse event = new ProfilesResponse(this.getProfiles(client));
                event.requestUUID = AbstractResponse.eventUUID;
                try {
                    webSocketHandler.sendMessage(session, event, false);
                } catch (IOException e) {
                    log.error("Error occurred during sending message. Cause: {}", e.getMessage());
                }
            });
        }
    }

    private final class ProfilesFileVisitor extends SimpleFileVisitor<Path> {

        private transient final Logger logger = LogManager.getLogger(ProfilesFileVisitor.class);

        private final Map<Path, ClientProfile> result;

        private ProfilesFileVisitor(Map<Path, ClientProfile> result) {
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            logger.info("Syncing '{}' profile", IOHelper.getFileName(file));

            // Read profile
            ClientProfile profile;
            try (BufferedReader reader = IOHelper.newReader(file)) {
                profile = jacksonManager.getMapper().readValue(reader, ClientProfile.class);
            }
            profile.verify();

            // Add SIGNED profile to result list
            result.put(file.toAbsolutePath(), profile);
            return super.visitFile(file, attrs);
        }
    }
}
