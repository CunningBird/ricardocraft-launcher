package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.MakeProfileHelper;
import ru.ricardocraft.backend.dto.updates.ServerProfile;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.profiles.ClientProfileBuilder;
import ru.ricardocraft.backend.profiles.ClientProfileVersions;

import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public final class DownloadClientCommand {

    private transient final DirectoriesManager directoriesManager;
    private transient final MirrorManager mirrorManager;
    private transient final UpdatesManager updatesManager;
    private transient final ProfileProvider profileProvider;
    private transient final ObjectMapper objectMapper;

    @ShellMethod("[version] [dir] (mirror/generate) Download client dir")
    public void downloadClient(@ShellOption String versionName,
                               @ShellOption(defaultValue = ShellOption.NULL) String dir,
                               @ShellOption(defaultValue = ShellOption.NULL) String downloadType) throws Exception {
        //Version version = Version.byName(versionName);
        String dirName = IOHelper.verifyFileName(dir != null ? dir : versionName);
        Path clientDir = directoriesManager.getUpdatesDir().resolve(dirName);

        boolean isMirrorClientDownload = false;
        if (downloadType != null) {
            isMirrorClientDownload = downloadType.equals("mirror");
        }

        // Download required client
        log.info("Downloading client, it may take some time");
        //HttpDownloader.downloadZip(server.mirrorManager.getDefaultMirror().getClientsURL(version.name), clientDir);
        mirrorManager.downloadZip(clientDir, "clients/%s.zip", versionName);

        // Create profile file
        log.info("Creaing profile file: '{}'", dirName);
        ClientProfile clientProfile = null;
        if (isMirrorClientDownload) {
            try {
                JsonNode clientJson = mirrorManager.jsonRequest(null, "GET", "clients/%s.json", versionName);
                clientProfile = objectMapper.readValue(clientJson.asText(), ClientProfile.class);
                var builder = new ClientProfileBuilder(clientProfile);
                builder.setTitle(dirName);
                builder.setDir(dirName);
                builder.setUuid(UUID.randomUUID());
                clientProfile = builder.createClientProfile();
                if (clientProfile.getServers() != null) {
                    ServerProfile serverProfile = clientProfile.getDefaultServerProfile();
                    if (serverProfile != null) {
                        serverProfile.name = dirName;
                    }
                }
            } catch (Exception e) {
                log.error("Filed download clientProfile from mirror: '{}' Generation through MakeProfileHelper", versionName);
                isMirrorClientDownload = false;
            }
        }
        if (!isMirrorClientDownload) {
            try {
                String internalVersion = versionName;
                if (internalVersion.contains("-")) {
                    internalVersion = internalVersion.substring(0, versionName.indexOf('-'));
                }
                Version version = Version.of(internalVersion);
                if (version.compareTo(ClientProfileVersions.MINECRAFT_1_7_10) <= 0) {
                    log.warn("Minecraft 1.7.9 and below not supported. Use at your own risk");
                }
                MakeProfileHelper.MakeProfileOption[] options = MakeProfileHelper.getMakeProfileOptionsFromDir(clientDir, version);
                for (MakeProfileHelper.MakeProfileOption option : options) {
                    log.debug("Detected option {}", option.getClass().getSimpleName());
                }
                clientProfile = MakeProfileHelper.makeProfile(version, dirName, options);
            } catch (Throwable e) {
                isMirrorClientDownload = true;
            }
        }
        profileProvider.addProfile(clientProfile);

        // Finished
        profileProvider.syncProfilesDir();
        updatesManager.syncUpdatesDir(Collections.singleton(dirName));
        log.info("Client successfully downloaded: '{}'", dirName);
    }
}
