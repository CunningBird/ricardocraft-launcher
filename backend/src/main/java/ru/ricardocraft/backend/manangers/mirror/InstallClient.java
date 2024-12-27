package ru.ricardocraft.backend.manangers.mirror;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import ru.ricardocraft.backend.auth.profiles.ProfileProvider;
import ru.ricardocraft.backend.base.Downloader;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.mirror.DeDupLibrariesCommand;
import ru.ricardocraft.backend.command.mirror.installers.FabricInstallerCommand;
import ru.ricardocraft.backend.command.mirror.installers.QuiltInstallerCommand;
import ru.ricardocraft.backend.command.updates.profile.MakeProfileCommand;
import ru.ricardocraft.backend.dto.updates.Version;
import ru.ricardocraft.backend.dto.updates.VersionType;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.JacksonManager;
import ru.ricardocraft.backend.manangers.UpdatesManager;
import ru.ricardocraft.backend.manangers.mirror.build.*;
import ru.ricardocraft.backend.manangers.mirror.modapi.CurseforgeAPI;
import ru.ricardocraft.backend.manangers.mirror.modapi.ModrinthAPI;
import ru.ricardocraft.backend.manangers.mirror.newforge.ForgeProfile;
import ru.ricardocraft.backend.manangers.mirror.newforge.ForgeProfileLibrary;
import ru.ricardocraft.backend.manangers.mirror.newforge.ForgeProfileModifier;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.profiles.ClientProfileVersions;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.config.BuildScriptProperties;
import ru.ricardocraft.backend.properties.config.MultiModProperties;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Component
public class InstallClient {
    private static final Logger logger = LoggerFactory.getLogger(InstallClient.class);

    private transient final Map<String, BuildInCommand> buildInCommands = new HashMap<>();

    private final transient LaunchServerProperties properties;
    private final transient DirectoriesManager directoriesManager;
    private final transient UpdatesManager updatesManager;
    private final transient JacksonManager jacksonManager;
    private final transient ProfileProvider profileProvider;
    private final transient ModrinthAPI modrinthAPI;
    private final transient CurseforgeAPI curseforgeApi;
    private final transient FabricInstallerCommand fabricInstallerCommand;
    private final transient QuiltInstallerCommand quiltInstallerCommand;
    private final transient DeDupLibrariesCommand deDupLibrariesCommand;
    private final transient MakeProfileCommand makeProfileCommand;

    @Autowired
    public InstallClient(LaunchServerProperties properties,
                         DirectoriesManager directoriesManager,
                         UpdatesManager updatesManager,
                         JacksonManager jacksonManager,
                         ProfileProvider profileProvider,
                         ModrinthAPI modrinthAPI,
                         CurseforgeAPI curseforgeApi,
                         FabricInstallerCommand fabricInstallerCommand,
                         QuiltInstallerCommand quiltInstallerCommand,
                         DeDupLibrariesCommand deDupLibrariesCommand,
                         MakeProfileCommand makeProfileCommand) {
        this.properties = properties;
        this.directoriesManager = directoriesManager;
        this.updatesManager = updatesManager;
        this.jacksonManager = jacksonManager;
        this.profileProvider = profileProvider;
        this.modrinthAPI = modrinthAPI;
        this.curseforgeApi = curseforgeApi;
        this.fabricInstallerCommand = fabricInstallerCommand;
        this.quiltInstallerCommand = quiltInstallerCommand;
        this.deDupLibrariesCommand = deDupLibrariesCommand;
        this.makeProfileCommand = makeProfileCommand;

        this.buildInCommands.put("%download", new DownloadCommand());
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
        logger.info("Download {} {} into {}", fileInfo.fileName(), url, path);
        try (InputStream input = IOHelper.newInput(url.toURL())) {
            IOHelper.transfer(input, path);
        }
        logger.info("{} downloaded", fileInfo.fileName());
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
        logger.info("Download {} {} into {}", file.filename(), url, path);
        try (InputStream input = IOHelper.newInput(url.toURL())) {
            IOHelper.transfer(input, path);
        }
        logger.info("{} downloaded", file.filename());
    }

    public void run(String name, Version version, List<String> mods, VersionType versionType) throws Exception {
        logger.info("Install client {} {}", version.toString(), versionType);
        Path originalMinecraftProfile = null;
        Path clientPath = directoriesManager.getUpdatesDir().resolve(name);
        Path fetchDir = directoriesManager.getMirrorHelperWorkspaceDir().resolve("clients").resolve("vanilla").resolve(version.toString());
        if (Files.notExists(fetchDir)) downloadVanillaTo(fetchDir, version);
        copyDir(fetchDir, clientPath, path -> !(path.toString().contains("icu4j-core-mojang") && versionType == VersionType.FORGE && version.compareTo(ClientProfileVersions.MINECRAFT_1_12_2) == 0));
        Path tmpFile = directoriesManager.getMirrorHelperWorkspaceDir().resolve("file.tmp");

        // Setup authlib
        Path pathToLauncherAuthlib = getPathToLauncherAuthlib(version);
        Path pathToOriginalAuthlib = findClientAuthlib(clientPath);
        logger.info("Found launcher authlib in {}", pathToLauncherAuthlib);
        logger.info("Found original authlib in {}", pathToOriginalAuthlib);
        merge2Jars(pathToOriginalAuthlib, pathToLauncherAuthlib, tmpFile);
        Files.delete(pathToOriginalAuthlib);
        Files.move(tmpFile, pathToOriginalAuthlib);
        logger.info("Authlib patched");

        // Apply mod engine
        if (versionType == VersionType.FABRIC) {
            if (properties.getMirror().getWorkspace().getFabricLoaderVersion() == null) {
                fabricInstallerCommand.invoke(version.toString(), name, directoriesManager.getMirrorHelperWorkspaceDir().resolve("installers").resolve("fabric-installer.jar").toAbsolutePath().toString());
            } else {
                fabricInstallerCommand.invoke(version.toString(), name, directoriesManager.getMirrorHelperWorkspaceDir().resolve("installers").resolve("fabric-installer.jar").toAbsolutePath().toString(), properties.getMirror().getWorkspace().getFabricLoaderVersion());
            }
            Files.createDirectories(clientPath.resolve("mods"));
            logger.info("Fabric installed");
        } else if (versionType == VersionType.QUILT) {
            quiltInstallerCommand.invoke(version.toString(), name, directoriesManager.getMirrorHelperWorkspaceDir().resolve("installers").resolve("quilt-installer.jar").toAbsolutePath().toString());
            Files.createDirectories(clientPath.resolve("mods"));
            logger.info("Quilt installed");
        } else if (versionType == VersionType.FORGE || versionType == VersionType.NEOFORGE) {
            String forgePrefix = versionType == VersionType.NEOFORGE ? "neoforge" : "forge";
            Path forgeInstaller = ResourceUtils.getFile("classpath:forge/" + forgePrefix + "-" + version + "-installer-nogui.jar").toPath();
            if (Files.notExists(forgeInstaller)) throw new FileNotFoundException(forgeInstaller.toAbsolutePath().toString());

            Path tmpDir = directoriesManager.getMirrorHelperWorkspaceDir().resolve("clients").resolve(forgePrefix).resolve(version.toString());
            if (Files.notExists(tmpDir)) {
                Files.createDirectories(tmpDir);
                Files.createDirectories(tmpDir.resolve("versions"));
                IOHelper.transfer("{\"profiles\": {}}".getBytes(StandardCharsets.UTF_8), tmpDir.resolve("launcher_profiles.json"), false);
                int counter = 5;
                do {
                    Process forgeProcess;
                    logger.info("Install forge client into {} (no gui)", tmpDir.toAbsolutePath());
                    forgeProcess = new ProcessBuilder()
                            .command("java", "-jar", forgeInstaller.toAbsolutePath().toString(), "--installClient", tmpDir.toAbsolutePath().toString())
                            .directory(tmpDir.toFile())
                            .inheritIO()
                            .start();
                    int code = forgeProcess.waitFor();
                    logger.info("Process return with status code {}", code);
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
            logger.debug("Forge profile {}", forgeProfileFile);
            ForgeProfile forgeProfile;
            try (Reader reader = IOHelper.newReader(forgeProfileFile)) {
                forgeProfile = jacksonManager.getMapper().readValue(reader, ForgeProfile.class);
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
                FabricInstallerCommand.NamedURL url = FabricInstallerCommand.makeURL(libUrl, libName);
                Path file = clientPath.resolve("libraries").resolve(url.name);
                IOHelper.createParentDirs(file);
                if (Files.exists(file)) {
                    continue;
                }
                logger.info("Download {} into {}", url.url.toString(), url.name);
                try {
                    try (InputStream stream = IOHelper.newInput(url.url)) {
                        try (OutputStream output = IOHelper.newOutput(file)) {
                            IOHelper.transfer(stream, output);
                        }
                    }
                } catch (FileNotFoundException e) {
                    logger.warn("Not found {}", url.url);
                }
            }
            if (properties.getMirror().getDeleteTmpDir()) {
                IOHelper.deleteDir(tmpDir, true);
            }
            Files.createDirectories(clientPath.resolve("mods"));
            logger.info("Forge installed");
        }

        // Mirror
        for (var entry : properties.getMirror().getWorkspace().getBuild().entrySet()) {
            var k = entry.getKey();
            var v = entry.getValue();
            if (!buildScriptCheck(v, versionType, version)) {
                continue;
            }
            Path target = directoriesManager.getMirrorHelperWorkspaceDir().resolve(v.getPath());
            if (entry.getValue().getDynamic() || Files.notExists(target)) {
                logger.info("Build {}", k);
                try {
                    build(k, v, clientPath);
                } catch (Throwable e) {
                    logger.error("Build error", e);
                }
            }
        }
        logger.info("Build required libraries");
        copyDir(directoriesManager.getMirrorHelperWorkspaceDir().resolve("workdir").resolve("ALL"), clientPath);
        copyDir(directoriesManager.getMirrorHelperWorkspaceDir().resolve("workdir").resolve(versionType.name()), clientPath);
        copyDir(directoriesManager.getMirrorHelperWorkspaceDir().resolve("workdir").resolve("lwjgl3"), clientPath);
        copyDir(directoriesManager.getMirrorHelperWorkspaceDir().resolve("workdir").resolve("java17"), clientPath);
        copyDir(directoriesManager.getMirrorHelperWorkspaceDir().resolve("workdir").resolve(version.toString()).resolve("ALL"), clientPath);
        copyDir(directoriesManager.getMirrorHelperWorkspaceDir().resolve("workdir").resolve(version.toString()).resolve(versionType.name()), clientPath);
        logger.info("Files copied");
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
                    logger.warn("Mod {} not installed! Exception {}", modId, e);
                }
            }
            logger.info("Mods installed");
        }
        logger.info("Install multiMods");
        for (var m : properties.getMirror().getWorkspace().getMultiMods().entrySet()) {
            var k = m.getKey();
            var v = m.getValue();
            if (!multiModCheck(v, versionType, version)) {
                continue;
            }
            Path file = directoriesManager.getMirrorHelperWorkspaceDir().resolve("multimods").resolve(k.concat(".jar"));
            if (Files.notExists(file)) {
                logger.warn("File {} not exist", file);
                continue;
            }
            Path targetMod = v.getTarget() != null ? clientPath.resolve(v.getTarget()) : clientPath.resolve("mods").resolve(file.getFileName());
            logger.info("Copy {} to {}", file, targetMod);
            IOHelper.copy(file, targetMod);
            logger.info("MultiMods installed");
        }
        deDupLibrariesCommand.invoke(clientPath.toAbsolutePath().toString(), "false");
        logger.info("deduplibraries completed");


        makeProfileCommand.invoke(name, version.toString(), name);
        logger.info("makeprofile completed");
        if ((versionType == VersionType.FORGE || versionType == VersionType.NEOFORGE) && version.compareTo(ClientProfileVersions.MINECRAFT_1_17) >= 0) {
            ClientProfile profile = profileProvider.getProfile(name);
            logger.info("Run ForgeProfileModifier");
            ForgeProfileModifier modifier = new ForgeProfileModifier(originalMinecraftProfile, profile, clientPath, jacksonManager);
            profile = modifier.build();
            profileProvider.addProfile(profile);
        }
        if (versionType == VersionType.FORGE && version.compareTo(ClientProfileVersions.MINECRAFT_1_12_2) == 0) {
            ClientProfile profile = profileProvider.getProfile(name);
            logger.info("Run ForgeProfileModifierCleanRoom");
            ForgeProfileModifier modifier = new ForgeProfileModifier(originalMinecraftProfile, profile, clientPath, jacksonManager);
            profile = modifier.buildCleanRoom();
            profileProvider.addProfile(profile);
        }
        updatesManager.syncUpdatesDir(Collections.singleton(name));
        logger.info("Completed");
    }

    private void downloadVanillaTo(Path clientDir, Version version) throws Exception {
        JsonNode obj;
        Path vanillaProfileJson = directoriesManager.getMirrorHelperWorkspaceDir().resolve("profiles").resolve("vanilla").resolve(version.toString().concat(".json"));
        if (Files.exists(vanillaProfileJson)) {
            logger.info("Using file {}", vanillaProfileJson);
            try (Reader reader = IOHelper.newReader(vanillaProfileJson)) {
                obj = jacksonManager.getMapper().readTree(reader);
            }
        } else {
            IOHelper.createParentDirs(vanillaProfileJson);
            obj = gainClient(version.toString());
            try (Writer writer = IOHelper.newWriter(vanillaProfileJson)) {
                jacksonManager.getMapper().writeValue(writer, obj);
            }
        }
        IOHelper.createParentDirs(clientDir);
        ClientInfo info = getClient(obj);
        // Download required files
        logger.info("Downloading client, it may take some time");
        ExecutorService e = Executors.newFixedThreadPool(4);
        //info.libraries.addAll(info.natives); // Hack
        List<Downloader.SizedFile> applies = info.libraries.stream()
                .filter(l -> !(l.name.contains("natives")))
                .map(y -> new Downloader.SizedFile(y.url, y.path, y.size)).collect(Collectors.toList());
        var downloader = Downloader.downloadList(applies, null, clientDir.resolve("libraries"), null, e, 4);
        if (info.client != null) {
            IOHelper.transfer(IOHelper.newInput(new URI(info.client.url).toURL()), clientDir.resolve("minecraft.jar"));
        }
        logger.info("Downloaded client jar!");
        downloader.getFuture().get();
        e.shutdownNow();
        // Finished
        logger.info("Client downloaded!");
    }

    public void build(String scriptName, BuildScriptProperties buildScript, Path clientDir) throws IOException {
        BuildContext context = new BuildContext();
        context.targetClientDir = clientDir;
        context.scriptBuildDir = context.createNewBuildDir(scriptName);
        context.update(properties.getProjectName());
        logger.info("Script build dir {}", context.scriptBuildDir);
        try {
            for (var inst : buildScript.getScript()) {
                var cmd = inst.getCmd().stream().map(context::replace).toList();
                logger.info("Execute {}", String.join(" ", cmd));
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
                var to = buildScript.getDynamic() ? clientDir.resolve(context.replace(buildScript.getPath())) : directoriesManager.getMirrorHelperWorkspaceDir().resolve(buildScript.getPath());
                logger.info("Copy {} to {}", from, to);
                IOHelper.createParentDirs(to);
                IOHelper.copy(from, to);
            }
            logger.info("Deleting temp dir {}", context.scriptBuildDir);
        } catch (Throwable e) {
            logger.error("Build {} failed: {}", scriptName, e);
        }
    }

    private Path getPathToLauncherAuthlib(Version version) throws FileNotFoundException {
        if (version.compareTo(ClientProfileVersions.MINECRAFT_1_16_5) < 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib1.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_18) < 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib2.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_19) < 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib3.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_19) == 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib3-1.19.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20) < 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib3-1.19.1.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20_2) < 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib4.jar");
        else if (version.compareTo(ClientProfileVersions.MINECRAFT_1_20_3) < 0) return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib5.jar");
        else return directoriesManager.getMirrorHelperWorkspaceDir().resolve("authlib").resolve("LauncherAuthlib6.jar");
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
            JsonNode obj = jacksonManager.getMapper().readTree(new URI("https://launchermeta.mojang.com/mc/game/version_manifest.json").toURL());

            if (obj.has("versions") && obj.get("versions").isArray())
                for (JsonNode el : obj.get("versions")) {
                    if (el != null && el.isObject()) {
                        JsonNode ver = el.get("id");
                        if (ver != null && mc.equals(ver.textValue()))
                            workURL = el.get("url").textValue();
                    }
                }
            if (workURL != null) {
                obj = jacksonManager.getMapper().readTree(IOHelper.request(new URI(workURL).toURL()));
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
                            Artifact a = jacksonManager.getMapper().readValue(p.getValue().toString(), Artifact.class);
                            a.name = p.getKey() + '/' + e.get("name").textValue();
                            ret.natives.add(a);
                        }
                    }
                } else if (downloads.has("artifact")) {
                    Artifact a = jacksonManager.getMapper().readValue(downloads.get("artifact").toString(), Artifact.class);
                    a.name = "art/" + e.get("name").textValue();
                    ret.libraries.add(a);
                }

            }
        }
        if (obj.has("downloads")) {
            JsonNode tmp = obj.get("downloads");
            ret.client = jacksonManager.getMapper().readValue(tmp.get("client").toString(), Downloadable.class);
            ret.server = jacksonManager.getMapper().readValue(tmp.get("server").toString(), Downloadable.class);
        }
        return ret;
    }
}
