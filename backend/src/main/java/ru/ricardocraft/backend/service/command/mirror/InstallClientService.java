package ru.ricardocraft.backend.service.command.mirror;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.dto.updates.VersionType;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.MirrorWorkspaceProperties;
import ru.ricardocraft.backend.service.mirror.InstallClient;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InstallClientService {

    private final LaunchServerProperties config;
    private final InstallClient installClient;

    public void installClient(String name, String clientVersion, String clientVersionType, @Nullable String[] clientMods) throws Exception {
        Version version = parseClientVersion(clientVersion);
        VersionType versionType = VersionType.valueOf(clientVersionType);
        List<String> mods = new ArrayList<>();
        MirrorWorkspaceProperties mirrorWorkspace = config.getMirror().getWorkspace();
        if (mirrorWorkspace != null) {
            switch (versionType) {
                case VANILLA -> {
                }
                case FABRIC -> mods.addAll(config.getMirror().getWorkspace().getFabricMods());
                case FORGE -> mods.addAll(config.getMirror().getWorkspace().getForgeMods());
                case QUILT -> mods.addAll(config.getMirror().getWorkspace().getQuiltMods());
            }
        }
        if (clientMods != null && clientMods.length > 0) {
            mods = List.of(clientMods);
        }
        installClient.run(name, version, mods, versionType);
    }

    protected Version parseClientVersion(String arg) throws Exception {
        if (arg.isEmpty()) throw new Exception("ClientVersion can't be empty");
        return Version.of(arg);
    }
}
