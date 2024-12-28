package ru.ricardocraft.backend.command.updates;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.MakeProfileHelper;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.profiles.ClientProfileBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public class ProfilesCommand {

    private final DirectoriesManager directoriesManager;
    private final ProfileProvider profileProvider;
    private final UpdatesManager updatesManager;
    private final UpdatesProvider updatesProvider;

    @ShellMethod("[profile title/uuid] [new profile title] clone profile and profile dir")
    public void profileClone(@ShellOption String profileUuid, @ShellOption String profileTitle) throws Exception {
        ClientProfile profile;
        try {
            UUID uuid = UUID.fromString(profileUuid);
            profile = profileProvider.getProfile(uuid);
        } catch (IllegalArgumentException ex) {
            profile = profileProvider.getProfile(profileUuid);
        }
        var builder = new ClientProfileBuilder(profile);
        builder.setTitle(profileTitle);
        builder.setUuid(UUID.randomUUID());
        if (profile.getServers().size() == 1) {
            profile.getServers().getFirst().name = profileTitle;
        }
        log.info("Copy {} to {}", profile.getDir(), profileTitle);
        var src = directoriesManager.getUpdatesDir().resolve(profile.getDir());
        var dest = directoriesManager.getUpdatesDir().resolve(profileTitle);
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> {
                try {
                    IOHelper.copy(source, dest.resolve(src.relativize(source)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        builder.setDir(profileTitle);
        profile = builder.createClientProfile();
        profileProvider.addProfile(profile);
        log.info("Profile {} cloned from {}", profileTitle, profileUuid);
        profileProvider.syncProfilesDir();
        updatesManager.syncUpdatesDir(List.of(profileTitle));
    }

    @ShellMethod("[uuid/title] permanently delete profile")
    public void profileDelete(@ShellOption String profileUuid) throws Exception {
        ClientProfile profile;
        try {
            UUID uuid = UUID.fromString(profileUuid);
            profile = profileProvider.getProfile(uuid);
        } catch (IllegalArgumentException ex) {
            profile = profileProvider.getProfile(profileUuid);
        }
        if (profile == null) {
            log.error("Profile {} not found", profileUuid);
            return;
        }
        log.warn("THIS ACTION DELETE PROFILE AND ALL FILES IN {}", profile.getDir());

        log.info("Delete {} ({})", profile.getTitle(), profile.getUUID());
        profileProvider.deleteProfile(profile);
        log.info("Delete {}", profile.getDir());
        updatesProvider.delete(profile.getDir());
    }

    @ShellMethod("show all profiles")
    public void profileList() {
        for (var profile : profileProvider.getProfiles()) {
            log.info("{} ({}) {}", profile.getTitle(), profile.getVersion().toString(), profile.isLimited() ? "limited" : "");
        }
    }

    @ShellMethod("[name] [minecraft version] [dir] make profile for any minecraft versions")
    public void profileMake(@ShellOption String name,
                            @ShellOption String minecraftVersion,
                            @ShellOption String dir) throws Exception {
        Version version = parseClientVersion(minecraftVersion);
        MakeProfileHelper.MakeProfileOption[] options = MakeProfileHelper.getMakeProfileOptionsFromDir(directoriesManager.getUpdatesDir().resolve(dir), version);
        for (MakeProfileHelper.MakeProfileOption option : options) {
            log.info("Detected option {}", option);
        }
        ClientProfile profile = MakeProfileHelper.makeProfile(version, name, options);
        profileProvider.addProfile(profile);
        log.info("Profile {} created", name);
        profileProvider.syncProfilesDir();
    }

    @ShellMethod("[profile names...] load and save profile")
    public void profileSave(@ShellOption String... args) throws Exception {
        if (args.length > 0) {
            for (String profileName : args) {
                ClientProfile profile;
                try {
                    UUID uuid = UUID.fromString(profileName);
                    profile = profileProvider.getProfile(uuid);
                } catch (IllegalArgumentException ex) {
                    profile = profileProvider.getProfile(profileName);
                }
                profileProvider.addProfile(profile);
            }
            profileProvider.syncProfilesDir();
        }
    }

    protected Version parseClientVersion(String arg) throws Exception {
        if (arg.isEmpty()) throw new Exception("ClientVersion can't be empty");
        return Version.of(arg);
    }
}
