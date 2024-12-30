package ru.ricardocraft.backend.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.ricardocraft.backend.service.command.updates.*;

import java.io.IOException;

@Slf4j
@ShellComponent
@ShellCommandGroup("updates")
@RequiredArgsConstructor
public class UpdatesCommands {

    private final DownloadAssetService downloadAssetService;
    private final DownloadClientService downloadClientService;
    private final IndexAssetService indexAssetService;
    private final UnindexAssetService unindexAssetService;
    private final ProfilesService profilesService;
    private final SyncService syncService;

    @ShellMethod("[version] [dir] (mojang/mirror) Download asset dir")
    public void downloadAsset(@ShellOption String versionName,
                              @ShellOption(defaultValue = ShellOption.NULL) String dir,
                              @ShellOption(defaultValue = ShellOption.NULL) String mirrorType) throws Exception {
        downloadAssetService.downloadAsset(versionName, dir, mirrorType);
    }

    @ShellMethod("[version] [dir] (mirror/generate) Download client dir")
    public void downloadClient(@ShellOption String versionName,
                               @ShellOption(defaultValue = ShellOption.NULL) String dir,
                               @ShellOption(defaultValue = ShellOption.NULL) String downloadType) throws Exception {
        downloadClientService.downloadClient(versionName, dir, downloadType);
    }

    @ShellMethod("[dir] [index] [output-dir] Index asset dir (1.7.10+)")
    public void indexAsset(@ShellOption String indexInputAssetDirName,
                           @ShellOption String indexIndexFileName,
                           @ShellOption String outputOutputAssetDirName) throws Exception {
        indexAssetService.indexAsset(indexInputAssetDirName, indexIndexFileName, outputOutputAssetDirName);
    }

    @ShellMethod("[dir] [index] [output-dir] Unindex asset dir (1.7.10+)")
    public void unindexAsset(@ShellOption String indexInputAssetDirName,
                             @ShellOption String indexIndexFileName,
                             @ShellOption String outputOutputAssetDirName) throws Exception {
        unindexAssetService.unindexAsset(indexInputAssetDirName, indexIndexFileName, outputOutputAssetDirName);
    }

    @ShellMethod("[profile title/uuid] [new profile title] clone profile and profile dir")
    public void profileClone(@ShellOption String profileUuid, @ShellOption String profileTitle) throws Exception {
        profilesService.profileClone(profileUuid, profileTitle);
    }

    @ShellMethod("[] Resync profiles dir")
    public void syncProfiles() throws IOException {
        syncService.syncProfiles();
    }

    @ShellMethod("[] sync updates cache")
    public void syncUpdatesCache() {
        syncService.syncUpdatesCache();
    }

    @ShellMethod("[] Resync profiles & updates dirs")
    public void syncUp() throws IOException {
        syncService.syncUp();
    }

    @ShellMethod("[subdirs...] Resync updates dir")
    public void syncUpdates(@ShellOption String... args) throws IOException {
        syncService.syncUpdates(args);
    }
}
