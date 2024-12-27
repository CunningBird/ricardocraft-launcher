package ru.ricardocraft.backend.command.updates;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.base.helper.MakeProfileHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.CommandException;
import ru.ricardocraft.backend.dto.updates.ServerProfile;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.MirrorManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.profiles.ClientProfileBuilder;
import ru.ricardocraft.backend.profiles.ClientProfileVersions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

@Component
public final class DownloadClientCommand extends Command {

    private transient final Logger logger = LogManager.getLogger(DownloadClientCommand.class);

    private transient final DirectoriesManager directoriesManager;
    private transient final MirrorManager mirrorManager;
    private transient final UpdatesManager updatesManager;
    private transient final ProfileProvider profileProvider;
    private transient final JacksonManager jacksonManager;

    @Autowired
    public DownloadClientCommand(DirectoriesManager directoriesManager,
                                 MirrorManager mirrorManager,
                                 UpdatesManager updatesManager,
                                 ProfileProvider profileProvider,
                                 JacksonManager jacksonManager) {
        super();
        this.directoriesManager = directoriesManager;
        this.mirrorManager = mirrorManager;
        this.updatesManager = updatesManager;
        this.profileProvider = profileProvider;
        this.jacksonManager = jacksonManager;
    }

    @Override
    public String getArgsDescription() {
        return "[version] [dir] (mirror/generate)";
    }

    @Override
    public String getUsageDescription() {
        return "Download client dir";
    }

    @Override
    public void invoke(String... args) throws IOException, CommandException {
        verifyArgs(args, 2);
        //Version version = Version.byName(args[0]);
        String versionName = args[0];
        String dirName = IOHelper.verifyFileName(args[1] != null ? args[1] : args[0]);
        Path clientDir = directoriesManager.getUpdatesDir().resolve(dirName);

        boolean isMirrorClientDownload = false;
        if (args.length > 2) {
            isMirrorClientDownload = args[2].equals("mirror");
        }

        // Download required client
        logger.info("Downloading client, it may take some time");
        //HttpDownloader.downloadZip(server.mirrorManager.getDefaultMirror().getClientsURL(version.name), clientDir);
        mirrorManager.downloadZip(clientDir, "clients/%s.zip", versionName);

        // Create profile file
        logger.info("Creaing profile file: '{}'", dirName);
        ClientProfile clientProfile = null;
        if (isMirrorClientDownload) {
            try {
                JsonNode clientJson = mirrorManager.jsonRequest(null, "GET", "clients/%s.json", versionName);
                clientProfile = jacksonManager.getMapper().readValue(clientJson.asText(), ClientProfile.class);
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
                logger.error("Filed download clientProfile from mirror: '{}' Generation through MakeProfileHelper", versionName);
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
                    logger.warn("Minecraft 1.7.9 and below not supported. Use at your own risk");
                }
                MakeProfileHelper.MakeProfileOption[] options = MakeProfileHelper.getMakeProfileOptionsFromDir(clientDir, version);
                for (MakeProfileHelper.MakeProfileOption option : options) {
                    logger.debug("Detected option {}", option.getClass().getSimpleName());
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
        logger.info("Client successfully downloaded: '{}'", dirName);
    }
}
