package ru.ricardocraft.backend.service.mirror;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import ru.ricardocraft.backend.base.SizedFile;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.command.mirror.DeDupLibrariesService;
import ru.ricardocraft.backend.service.command.mirror.LaunchInstallerFabricService;
import ru.ricardocraft.backend.service.command.mirror.LaunchInstallerQuiltService;
import ru.ricardocraft.backend.service.command.updates.ProfilesService;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.dto.updates.VersionType;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.BuildScriptProperties;
import ru.ricardocraft.backend.properties.config.MultiModProperties;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.UpdatesService;
import ru.ricardocraft.backend.service.mirror.build.*;
import ru.ricardocraft.backend.service.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.backend.service.mirror.modapi.ModrinthAPI;
import ru.ricardocraft.backend.service.mirror.newforge.ForgeProfile;
import ru.ricardocraft.backend.service.mirror.newforge.ForgeProfileLibrary;
import ru.ricardocraft.backend.service.mirror.newforge.ForgeProfileModifier;
import ru.ricardocraft.backend.service.profiles.ClientProfile;
import ru.ricardocraft.backend.service.profiles.ClientProfileVersions;
import ru.ricardocraft.backend.service.profiles.ProfileProvider;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
public class InstallClient {

    private final Map<String, BuildInCommand> buildInCommands = new HashMap<>();

    private final LaunchServerProperties properties;
    private final RestTemplate restTemplate;
    private final DirectoriesService directoriesService;
    private final UpdatesService updatesService;
    private final ObjectMapper objectMapper;
    private final ProfileProvider profileProvider;
    private final ModrinthAPI modrinthAPI;
    private final CurseforgeAPI curseforgeApi;
    private final LaunchInstallerFabricService launchInstallerFabricService;
    private final LaunchInstallerQuiltService launchInstallerQuiltService;
    private final DeDupLibrariesService deDupLibrariesService;
    private final ProfilesService profilesService;

    @Autowired
    public InstallClient(LaunchServerProperties properties,
                         RestTemplate restTemplate,
                         DirectoriesService directoriesService,
                         UpdatesService updatesService,
                         ObjectMapper objectMapper,
                         ProfileProvider profileProvider,
                         ModrinthAPI modrinthAPI,
                         CurseforgeAPI curseforgeApi,
                         LaunchInstallerFabricService launchInstallerFabricService,
                         LaunchInstallerQuiltService launchInstallerQuiltService,
                         DeDupLibrariesService deDupLibrariesService,
                         ProfilesService profilesService) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.directoriesService = directoriesService;
        this.updatesService = updatesService;
        this.objectMapper = objectMapper;
        this.profileProvider = profileProvider;
        this.modrinthAPI = modrinthAPI;
        this.curseforgeApi = curseforgeApi;
        this.launchInstallerFabricService = launchInstallerFabricService;
        this.launchInstallerQuiltService = launchInstallerQuiltService;
        this.deDupLibrariesService = deDupLibrariesService;
        this.profilesService = profilesService;

        this.buildInCommands.put("%download", new DownloadCommand(restTemplate));
        this.buildInCommands.put("%findJar", new FindJar());
        this.buildInCommands.put("%fetchManifestValue", new FetchManifestValue());
        this.buildInCommands.put("%if", new If());
        this.buildInCommands.put("%updateGradle", new UpdateGradle());
    }

    public void installMod(Path modsDir, long modId, Version version) throws Exception {
        var modInfo = curseforgeApi.fetchModById(modId);
        long fileId = modInfo.findFileIdByGameVersion(version.toString());
        var fileInfo = curseforgeApi.fetchModFileById(modId, fileId);
        URI url = new URI(fileInfo.downloadUrl());
        Path path = modsDir.resolve(fileInfo.fileName().replace("+", "-"));
        log.info("Download {} {} into {}", fileInfo.fileName(), url, path);
        try (InputStream input = IOHelper.newInput(url.toURL())) {
            IOHelper.transfer(input, path);
        }
        log.info("{} downloaded", fileInfo.fileName());
    }

    public void installMod(Path modsDir, String slug, String loader, Version version) throws Exception {
        var list = modrinthAPI.getMod(slug);
        var mod = modrinthAPI.getModByGameVersion(list, version.toString(), loader);
        if (mod == null) {
            throw new RuntimeException("Mod '%s' not supported game version '%s'".formatted(slug, version.toString()));
        }
        ModrinthAPI.ModVersionFileData file = null;
        for (var e : mod.files()) {
            file = e;
            if (e.primary()) {
                break;
            }
        }
        if (file == null) {
            throw new RuntimeException("Mod '%s' not found suitable file".formatted(slug));
        }
        URI url = new URI(file.url());
        Path path = modsDir.resolve(file.filename().replace("+", "-"));
        log.info("Download {} {} into {}", file.filename(), url, path);
        try (InputStream input = IOHelper.newInput(url.toURL())) {
            IOHelper.transfer(input, path);
        }
        log.info("{} downloaded", file.filename());
    }

    public void run(String name, Version version, List<String> mods, VersionType versionType) throws Exception {
        log.info("Install client {} {}", version.toString(), versionType);
        Path originalMinecraftProfile = null;
        Path clientPath = directoriesService.getUpdatesDir().resolve(name);
        Path fetchDir = directoriesService.getMirrorHelperWorkspaceDir().resolve("clients").resolve("vanilla").resolve(version.toString());
        if (Files.notExists(fetchDir)) downloadVanillaTo(fetchDir, version);
        copyDir(fetchDir, clientPath, path -> !(path.toString().contains("icu4j-core-mojang") && versionType == VersionType.FORGE && version.compareTo(ClientProfileVersions.MINECRAFT_1_12_2) == 0));
        Path tmpFile = directoriesService.getMirrorHelperWorkspaceDir().resolve("file.tmp");

        // Setup authlib
        Path pathToLauncherAuthlib = getPathToLauncherAuthlib(version);
        Path pathToOriginalAuthlib = findClientAuthlib(clientPath);
        log.info("Found launcher authlib in {}", pathToLauncherAuthlib);
        log.info("Found original authlib in {}", pathToOriginalAuthlib);
        merge2Jars(pathToOriginalAuthlib, pathToLauncherAuthlib, tmpFile);
        Files.delete(pathToOriginalAuthlib);
        Files.move(tmpFile, pathToOriginalAuthlib);
        log.info("Authlib patched");

        // Apply mod engine
        if (versionType == VersionType.FABRIC) {
            if (properties.getMirror().getWorkspace().getFabricLoaderVersion() == null) {
                launchInstallerFabricService.launchInstallerFabric(
                        version.toString(),
                        name,
                        directoriesService.getMirrorHelperWorkspaceDir().resolve("installers").resolve("fabric-installer.jar").toAbsolutePath().toString(),
                        null
                );
            } else {
                launchInstallerFabricService.launchInstallerFabric(
                        version.toString(),
                        name,
                        directoriesService.getMirrorHelperWorkspaceDir().resolve("installers").resolve("fabric-installer.jar").toAbsolutePath().toString(),
                        properties.getMirror().getWorkspace().getFabricLoaderVersion()
                );
            }
            Files.createDirectories(clientPath.resolve("mods"));
            log.info("Fabric installed");
        } else if (versionType == VersionType.QUILT) {
            launchInstallerQuiltService.launchInstallerQuilit(
                    version.toString(),
                    name,
                    directoriesService.getMirrorHelperWorkspaceDir().resolve("installers").resolve("quilt-installer.jar").toAbsolutePath().toString()
            );
            Files.createDirectories(clientPath.resolve("mods"));
            log.info("Quilt installed");
        } else if (versionType == VersionType.FORGE || versionType == VersionType.NEOFORGE) {
            String forgePrefix = versionType == VersionType.NEOFORGE ? "neoforge" : "forge";
            Path forgeInstaller = ResourceUtils.getFile("classpath:forge/" + forgePrefix + "-" + version + "-installer-nogui.jar").toPath();
            if (Files.notExists(forgeInstaller))
                throw new FileNotFoundException(forgeInstaller.toAbsolutePath().toString());

            Path tmpDir = directoriesService.getMirrorHelperWorkspaceDir().resolve("clients").resolve(forgePrefix).resolve(version.toString());
            if (Files.notExists(tmpDir)) {
                Files.createDirectories(tmpDir);
                Files.createDirectories(tmpDir.resolve("versions"));
                IOHelper.transfer("{\"profiles\": {}}".getBytes(StandardCharsets.UTF_8), tmpDir.resolve("launcher_profiles.json"), false);
                int counter = 5;
                do {
                    Process forgeProcess;
                    log.info("Install forge client into {} (no gui)", tmpDir.toAbsolutePath());
                    forgeProcess = new ProcessBuilder()
                            .command("java", "-jar", forgeInstaller.toAbsolutePath().toString(), "--installClient", tmpDir.toAbsolutePath().toString())
                            .directory(tmpDir.toFile())
                            .inheritIO()
                            .start();
                    int code = forgeProcess.waitFor();
                    log.info("Process return with status code {}", code);
                    counter--;
                    if (counter <= 0) {
                        IOHelper.deleteDir(tmpDir, true);
                        throw new RuntimeException("Forge not installed");
                    }
                } while (!Files.isDirectory(tmpDir.resolve("libraries")));
            }
            copyDir(tmpDir.resolve("libraries"), clientPath.resolve("libraries"));

            Path forgeClientDir;
            try (Stream<Path> stream = Files.list(tmpDir.resolve("versions"))
                    .filter(x -> {
                        String fname = x.getFileName().toString().toLowerCase(Locale.ROOT);
                        return fname.contains("forge") || fname.contains("cleanroom");
                    })) {
                forgeClientDir = stream.findFirst().orElseThrow();
            }


            Path forgeProfileFile;
            try (Stream<Path> stream = Files.list(forgeClientDir).filter(p -> p.getFileName().toString().endsWith(".json"))) {
                forgeProfileFile = stream.findFirst().orElseThrow();
            }


            originalMinecraftProfile = forgeProfileFile;
            log.debug("Forge profile {}", forgeProfileFile);
            ForgeProfile forgeProfile;
            try (Reader reader = IOHelper.newReader(forgeProfileFile)) {
                forgeProfile = objectMapper.readValue(reader, ForgeProfile.class);
            }
            for (ForgeProfileLibrary library : forgeProfile.getLibraries()) {
                String libUrl = library.getDownloads() == null ? null : library.getDownloads().getArtifact().getUrl();
                String libName = library.getName();
                if (libUrl == null || libUrl.isEmpty()) {
                    libUrl = "https://libraries.minecraft.net/";
                }
                if (libName.endsWith("@jar")) {
                    libName = libName.substring(0, libName.length() - 4);
                }
                LaunchInstallerFabricService.NamedURL url = LaunchInstallerFabricService.makeURL(libUrl, libName);
                Path file = clientPath.resolve("libraries").resolve(url.name);
                IOHelper.createParentDirs(file);
                if (Files.exists(file)) {
                    continue;
                }
                log.info("Download {} into {}", url.url.toString(), url.name);
                try {
                    try (InputStream stream = IOHelper.newInput(url.url)) {
                        try (OutputStream output = IOHelper.newOutput(file)) {
                            IOHelper.transfer(stream, output);
                        }
                    }
                } catch (FileNotFoundException e) {
                    log.warn("Not found {}", url.url);
                }
            }
            if (properties.getMirror().getDeleteTmpDir()) {
                IOHelper.deleteDir(tmpDir, true);
            }
            Files.createDirectories(clientPath.resolve("mods"));
            log.info("Forge installed");
        }

        // Mirror
        for (var entry : properties.getMirror().getWorkspace().getBuild().entrySet()) {
            var k = entry.getKey();
            var v = entry.getValue();
            if (!buildScriptCheck(v, versionType, version)) {
                continue;
            }
            Path target = directoriesService.getMirrorHelperWorkspaceDir().resolve(v.getPath());
            if (entry.getValue().getDynamic() || Files.notExists(target)) {
                log.info("Build {}", k);
                try {
                    build(k, v, clientPath);
                } catch (Throwable e) {
                    log.error("Build error", e);
                }
            }
        }
        log.info("Build required libraries");
        copyDir(directoriesService.getMirrorHelperWorkspaceDir().resolve("workdir").resolve("ALL"), clientPath);
        copyDir(directoriesService.getMirrorHelperWorkspaceDir().resolve("workdir").resolve(versionType.name()), clientPath);
        copyDir(directoriesService.getMirrorHelperWorkspaceDir().resolve("workdir").resolve("lwjgl3"), clientPath);
        copyDir(directoriesService.getMirrorHelperWorkspaceDir().resolve("workdir").resolve("java17"), clientPath);
        copyDir(directoriesService.getMirrorHelperWorkspaceDir().resolve("workdir").resolve(version.toString()).resolve("ALL"), clientPath);
        copyDir(directoriesService.getMirrorHelperWorkspaceDir().resolve("workdir").resolve(version.toString()).resolve(versionType.name()), clientPath);
        log.info("Files copied");
        if (mods != null && !mods.isEmpty()) {
            Path modsDir = clientPath.resolve("mods");
            String loaderName = switch (versionType) {
                case VANILLA -> "";
                case FABRIC -> "fabric";
                case NEOFORGE -> "neoforge";
                case FORGE -> "forge";
                case QUILT -> "quilt";
            };
            for (var modId : mods) {
                try {
                    try {
                        long id = Long.parseLong(modId);
                        installMod(modsDir, id, version);
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                    installMod(modsDir, modId, loaderName, version);
                } catch (Throwable e) {
                    log.warn("Mod {} not installed! Exception {}", modId, e.getMessage());
                }
            }
            log.info("Mods installed");
        }
        log.info("Install multiMods");
        for (var m : properties.getMirror().getWorkspace().getMultiMods().entrySet()) {
            var k = m.getKey();
            var v = m.getValue();
            if (!multiModCheck(v, versionType, version)) {
                continue;
            }
            Path file = directoriesService.getMirrorHelperWorkspaceDir().resolve("multimods").resolve(k.concat(".jar"));
            if (Files.notExists(file)) {
                log.warn("File {} not exist", file);
                continue;
            }
            Path targetMod = v.getTarget() != null ? clientPath.resolve(v.getTarget()) : clientPath.resolve("mods").resolve(file.getFileName());
            log.info("Copy {} to {}", file, targetMod);
            IOHelper.copy(file, targetMod);
            log.info("MultiMods installed");
        }
        deDupLibrariesService.deDupLibraries(clientPath.toAbsolutePath().toString(), false);
        log.info("deduplibraries completed");


        profilesService.profileMake(name, version.toString(), name);
        log.info("makeprofile completed");
        if ((versionType == VersionType.FORGE || versionType == VersionType.NEOFORGE) && version.compareTo(ClientProfileVersions.MINECRAFT_1_17) >= 0) {
            ClientProfile profile = profileProvider.getProfile(name);
            log.info("Run ForgeProfileModifier");
            ForgeProfileModifier modifier = new ForgeProfileModifier(originalMinecraftProfile, profile, clientPath, objectMapper);
            profile = modifier.build();
            profileProvider.addProfile(profile);
        }
        if (versionType == VersionType.FORGE && version.compareTo(ClientProfileVersions.MINECRAFT_1_12_2) == 0) {
            ClientProfile profile = profileProvider.getProfile(name);
            log.info("Run ForgeProfileModifierCleanRoom");
            ForgeProfileModifier modifier = new ForgeProfileModifier(originalMinecraftProfile, profile, clientPath, objectMapper);
            profile = modifier.buildCleanRoom();
            profileProvider.addProfile(profile);
        }
        updatesService.syncUpdatesDir(Collections.singleton(name));
        log.info("Completed");
    }

    private void downloadVanillaTo(Path clientDir, Version version) throws Exception {
        JsonNode obj;
        Path vanillaProfileJson = directoriesService.getMirrorHelperWorkspaceDir().resolve("profiles").resolve("vanilla").resolve(version.toString().concat(".json"));
        if (Files.exists(vanillaProfileJson)) {
            log.info("Using file {}", vanillaProfileJson);
            try (Reader reader = IOHelper.newReader(vanillaProfileJson)) {
                obj = objectMapper.readTree(reader);
            }
        } else {
            IOHelper.createParentDirs(vanillaProfileJson);
            obj = gainClient(version.toString());
            try (Writer writer = IOHelper.newWriter(vanillaProfileJson)) {
                objectMapper.writeValue(writer, obj);
            }
        }
        IOHelper.createParentDirs(clientDir);
        ClientInfo info = getClient(obj);
        // Download required files
        log.info("Downloading client, it may take some time");

        //info.libraries.addAll(info.natives); // Hack
        List<SizedFile> applies = info.libraries.stream()
                .filter(l -> !(l.name.contains("natives")))
                .map(y -> new SizedFile(y.url, y.path, y.size)).collect(Collectors.toList());

//        // TODO multi-thread downloading
        Collections.shuffle(applies);
        Path librariesPath = clientDir.resolve("libraries");
        for (SizedFile file : applies) {
            Path filePath = librariesPath.resolve(file.filePath);
            IOHelper.createParentDirs(filePath);
            try {
                URI uri = new URI(file.urlPath);
                File ret = new File(String.valueOf(filePath));
                ret.createNewFile();
                log.info("Download apply {}", uri);
                restTemplate.execute(uri, HttpMethod.GET, null, clientHttpResponse -> {
                    FileOutputStream fileIO = new FileOutputStream(ret);
                    StreamUtils.copy(clientHttpResponse.getBody(), fileIO);
                    fileIO.close();
                    return ret;
                });
            } catch (Exception exception) {
                log.error("Failed to download {}: Cause {}", file.urlPath != null ? file.urlPath : file.filePath, exception.getMessage());
            }
        }

        if (info.client != null) {
            IOHelper.transfer(IOHelper.newInput(new URI(info.client.url).toURL()), clientDir.resolve("minecraft.jar"));
        }
        log.info("Downloaded client jar!");
        // Finished
        log.info("Client downloaded!");
    }

    public void build(String scriptName, BuildScriptProperties buildScript, Path clientDir) throws IOException {
        BuildContext context = new BuildContext();
        context.targetClientDir = clientDir;
        context.scriptBuildDir = context.createNewBuildDir(scriptName);
        context.update(properties.getProjectName());
        log.info("Script build dir {}", context.scriptBuildDir);
        try {
            for (var inst : buildScript.getScript()) {
                var cmd = inst.getCmd().stream().map(context::replace).toList();
                log.info("Execute {}", String.join(" ", cmd));
                var workdirString = context.replace(inst.getWorkdir());
                Path workdir = workdirString != null ? Path.of(workdirString) : context.scriptBuildDir;
                if (!cmd.isEmpty() && cmd.getFirst().startsWith("%")) {
                    BuildInCommand buildInCommand = buildInCommands.get(cmd.getFirst());
                    if (buildInCommand == null) {
                        throw new IllegalArgumentException(String.format("Build-in command %s not found", cmd.getFirst()));
                    }
                    List<String> cmdArgs = cmd.subList(1, cmd.size());
                    buildInCommand.run(cmdArgs, context, workdir);
                } else {
                    ProcessBuilder builder = new ProcessBuilder(cmd);
                    builder.inheritIO();
                    builder.directory(workdir.toFile());
                    Process process = builder.start();
                    int code = process.waitFor();
                    if (!inst.getIgnoreErrorCode() && code != 0) {
                        throw new RuntimeException(String.format("Process exited with code %d", code));
                    }
                }
            }
            if (buildScript.getResult() != null && buildScript.getPath() != null) {
                var from = Path.of(context.replace(buildScript.getResult()));
                var to = buildScript.getDynamic() ? clientDir.resolve(context.replace(buildScript.getPath())) : directoriesService.getMirrorHelperWorkspaceDir().resolve(buildScript.getPath());
                log.info("Copy {} to {}", from, to);
                IOHelper.createParentDirs(to);
                IOHelper.copy(from, to);
            }
            log.info("Deleting temp dir {}", context.scriptBuildDir);
        } catch (Throwable e) {
            log.error("Build {} failed: {}", scriptName, e.getMessage());
        }
    }

    private Path getPathToLauncherAuthlib(Version version) {
        if (version.compareTo(ClientProfileVersions.MINECRAFT_1_16_5) < 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib1.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_18) < 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib2.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_19) < 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib3.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_19) == 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib3-1.19.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20) < 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib3-1.19.1.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20_2) < 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib4.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20_3) < 0)
            return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib5.jar");
        else return directoriesService.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib6.jar");
    }

    private void copyDir(Path source, Path target) throws IOException {
        copyDir(source, target, path -> true);
    }

    private void copyDir(Path source, Path target, Predicate<Path> predicate) throws IOException {
        if (Files.notExists(source)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(source)) {
            stream.filter(e -> predicate.test(source.relativize(e))).forEach(src -> {
                try {
                    Path dest = target.resolve(source.relativize(src));
                    if (Files.isDirectory(src)) {
                        if (Files.notExists(dest)) {
                            Files.createDirectories(dest);
                        }
                    } else {
                        IOHelper.copy(src, target.resolve(source.relativize(src)));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private Path findClientAuthlib(Path clientDir) throws IOException {
        try (Stream<Path> stream = Files.walk(clientDir).filter(p -> !Files.isDirectory(p) && p.getFileName().toString().startsWith("authlib-"))) {
            return stream.findFirst().orElseThrow();
        }
    }

    private void merge2Jars(Path source1, Path source2, Path target) throws IOException {
        try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(target))) {
            Set<String> blacklist = new HashSet<>();
            try (ZipInputStream input = IOHelper.newZipInput(source2)) {
                ZipEntry e = input.getNextEntry();
                while (e != null) {
                    if (e.getName().startsWith("META-INF")) {
                        e = input.getNextEntry();
                        continue;
                    }
                    blacklist.add(e.getName());
                    ZipEntry newEntry = IOHelper.newZipEntry(e);
                    output.putNextEntry(newEntry);
                    IOHelper.transfer(input, output);
                    e = input.getNextEntry();
                }
            }
            try (ZipInputStream input = IOHelper.newZipInput(source1)) {
                ZipEntry e = input.getNextEntry();
                while (e != null) {
                    if (blacklist.contains(e.getName())) {
                        e = input.getNextEntry();
                        continue;
                    }
                    blacklist.add(e.getName());
                    ZipEntry newEntry = IOHelper.newZipEntry(e);
                    output.putNextEntry(newEntry);
                    IOHelper.transfer(input, output);
                    e = input.getNextEntry();
                }
            }
        }
    }

    private boolean multiModCheck(MultiModProperties multiMod, VersionType type, Version version) {
        if (multiMod.getType() != null && multiMod.getType() != type) {
            return false;
        }
        if (multiMod.getMinVersion() != null && version.compareTo(multiMod.getMinVersion()) < 0) {
            return false;
        }
        if (multiMod.getMaxVersion() != null && version.compareTo(multiMod.getMaxVersion()) > 0) {
            return false;
        }
        return true;
    }

    private boolean buildScriptCheck(BuildScriptProperties buildScript, VersionType type, Version version) {
        if (buildScript.getType() != null && buildScript.getType() != type) {
            return false;
        }
        if (buildScript.getMinVersion() != null && version.compareTo(buildScript.getMinVersion()) < 0) {
            return false;
        }
        if (buildScript.getMaxVersion() != null && version.compareTo(buildScript.getMaxVersion()) > 0) {
            return false;
        }
        return true;
    }

    private JsonNode gainClient(String mc) throws IOException {
        try {
            String workURL = null;
            JsonNode obj = objectMapper.readTree(new URI("https://launchermeta.mojang.com/mc/game/version_manifest.json").toURL());

            if (obj.has("versions") && obj.get("versions").isArray())
                for (JsonNode el : obj.get("versions")) {
                    if (el != null && el.isObject()) {
                        JsonNode ver = el.get("id");
                        if (ver != null && mc.equals(ver.textValue()))
                            workURL = el.get("url").textValue();
                    }
                }
            if (workURL != null) {
                obj = objectMapper.readTree(IOHelper.request(new URI(workURL).toURL()));
                return obj;
            }
            throw new IOException("Client not found");
        } catch (JsonProcessingException | MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ClientInfo getClient(JsonNode obj) throws JsonProcessingException {
        ClientInfo ret = new ClientInfo();
        for (JsonNode e : obj.get("libraries")) {
            if (e.isObject() && e.has("downloads")) {
                JsonNode downloads = e.get("downloads");
                if (downloads.has("classifiers")) {
                    for (Iterator<Map.Entry<String, JsonNode>> it = downloads.get("classifiers").fields(); it.hasNext(); ) {
                        Map.Entry<String, JsonNode> p = it.next();
                        if (p.getValue().isObject() && p.getKey().startsWith("native")) {
                            Artifact a = objectMapper.readValue(p.getValue().toString(), Artifact.class);
                            a.name = p.getKey() + '/' + e.get("name").textValue();
                            ret.natives.add(a);
                        }
                    }
                } else if (downloads.has("artifact")) {
                    Artifact a = objectMapper.readValue(downloads.get("artifact").toString(), Artifact.class);
                    a.name = "art/" + e.get("name").textValue();
                    ret.libraries.add(a);
                }

            }
        }
        if (obj.has("downloads")) {
            JsonNode tmp = obj.get("downloads");
            ret.client = objectMapper.readValue(tmp.get("client").toString(), Downloadable.class);
            ret.server = objectMapper.readValue(tmp.get("server").toString(), Downloadable.class);
        }
        return ret;
    }
}
