package ru.ricardocraft.backend.auth.profiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.protect.ProtectHandler;
import ru.ricardocraft.backend.auth.protect.interfaces.ProfilesProtectHandler;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.base.events.RequestEvent;
import ru.ricardocraft.backend.base.events.request.ProfilesRequestEvent;
import ru.ricardocraft.backend.base.profiles.ClientProfile;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.WebSocketService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

@Component
public class LocalProfileProvider extends ProfileProvider {

    public String profilesDir = "profiles";
    private transient volatile Map<Path, ClientProfile> profilesMap;
    private transient volatile Set<ClientProfile> profilesList; // Cache

    private final transient LaunchServerProperties properties;
    private final transient ProtectHandler handler;
    private final transient WebSocketService service;

    @Autowired
    public LocalProfileProvider(LaunchServerProperties properties, ProtectHandler handler, WebSocketService service) {
        this.properties = properties;
        this.handler = handler;
        this.service = service;
    }

    @Override
    public void sync() throws IOException {
        Path profilesDirPath = Path.of(profilesDir);
        if (!IOHelper.isDir(profilesDirPath))
            Files.createDirectory(profilesDirPath);
        Map<Path, ClientProfile> newProfiles = new HashMap<>();
        IOHelper.walk(profilesDirPath, new ProfilesFileVisitor(newProfiles), false);
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
        Path profilesDirPath = Path.of(profilesDir);
        ClientProfile oldProfile;
        Path target = null;
        for (var e : profilesMap.entrySet()) {
            if (e.getValue().getUUID().equals(profile.getUUID())) {
                target = e.getKey();
            }
        }
        if (target == null) {
            target = profilesDirPath.resolve(profile.getTitle() + ".json");
            oldProfile = profilesMap.get(target);
            if (oldProfile != null && !oldProfile.getUUID().equals(profile.getUUID())) {
                throw new FileAlreadyExistsException(target.toString());
            }
        }
        try (BufferedWriter writer = IOHelper.newWriter(target)) {
            Launcher.gsonManager.configGson.toJson(profile, writer);
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
        this.sync();
        if (properties.getNetty().getSendProfileUpdatesEvent()) {
            service.forEachActiveChannels((ch, handler) -> {
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

    private static final class ProfilesFileVisitor extends SimpleFileVisitor<Path> {
        private final Map<Path, ClientProfile> result;
        private final Logger logger = LogManager.getLogger();

        private ProfilesFileVisitor(Map<Path, ClientProfile> result) {
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            logger.info("Syncing '{}' profile", IOHelper.getFileName(file));

            // Read profile
            ClientProfile profile;
            try (BufferedReader reader = IOHelper.newReader(file)) {
                profile = Launcher.gsonManager.gson.fromJson(reader, ClientProfile.class);
            }
            profile.verify();

            // Add SIGNED profile to result list
            result.put(file.toAbsolutePath(), profile);
            return super.visitFile(file, attrs);
        }
    }
}
