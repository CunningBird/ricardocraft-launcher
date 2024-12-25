package ru.ricardocraft.backend.manangers.mirror.newforge;

import ru.ricardocraft.backend.base.LaunchOptions;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.profiles.ClientProfile;
import ru.ricardocraft.backend.profiles.ClientProfileBuilder;
import ru.ricardocraft.backend.dto.updates.ClassLoaderConfig;
import ru.ricardocraft.backend.dto.updates.CompatibilityFlags;
import ru.ricardocraft.backend.manangers.JacksonManager;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class ForgeProfileModifier {
    private final ForgeProfile forgeProfile;
    private final ClientProfile profile;
    private final Path clientDir;
    public static List<String> exclusionList = List.of("AutoRenamingTool", "net/minecraft/client");
    private static List<String> prevArgsList = List.of("-p", "--add-modules", "--add-opens", "--add-exports");

    public ForgeProfileModifier(Path forgeProfilePath, ClientProfile profile, Path clientDir, JacksonManager jacksonManager) {
        try (Reader reader = IOHelper.newReader(forgeProfilePath)) {
            this.forgeProfile = jacksonManager.getMapper().readValue(reader, ForgeProfile.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.profile = profile;
        this.clientDir = clientDir;
    }

    public boolean containsInExclusionList(String value) {
        for (var e : exclusionList) {
            if (value.contains(e)) {
                return true;
            }
        }
        return false;
    }

    public ClientProfile build() throws IOException {
        ClientProfileBuilder builder = new ClientProfileBuilder(profile);
        builder.setMainClass(forgeProfile.getMainClass());
        List<String> cp = new ArrayList<>(32);
        Path librariesPath = clientDir.resolve("libraries");
        try (Stream<Path> stream = Files.walk(librariesPath)) {
            cp.addAll(stream
                    .filter(e -> e.getFileName().toString().endsWith(".jar"))
                    .map(e -> clientDir.relativize(e).toString())
                    .filter(e -> !containsInExclusionList(e)).toList());
        }
        builder.setClassPath(cp);
        builder.setClassLoaderConfig(ClassLoaderConfig.LAUNCHER);
        builder.setFlags(List.of(CompatibilityFlags.ENABLE_HACKS));
        LaunchOptions.ModuleConf conf = new LaunchOptions.ModuleConf();
        List<String> jvmArgs = new ArrayList<>(forgeProfile.getArguments().getJvm().stream().map(this::processPlaceholders).toList());
        AtomicReference<String> prevArg = new AtomicReference<>();
        jvmArgs.removeIf(arg -> {
            if (prevArgsList.contains(arg)) {
                prevArg.set(arg);
                return true;
            }
            if (prevArg.get() != null) {
                processArg(prevArg.get(), arg, conf);
                prevArg.set(null);
                return true;
            }
            return false;
        });
        jvmArgs.add("--add-opens");
        jvmArgs.add("java.base/java.lang.invoke=ALL-UNNAMED");
        builder.setJvmArgs(jvmArgs);
        builder.setClientArgs(new ArrayList<>(forgeProfile.getArguments().getGame()));
//        List<String> compatClasses = new ArrayList<>();
//        for(var e : cp) {
//            if(e.toLowerCase().contains("filesystemfixer")) {
//                compatClasses.add("pro.gravit.compat.filesystem.FileSystemFixer");
//            }
//        }
//        builder.setCompatClasses(compatClasses);
        builder.setCompatClasses(List.of("pro.gravit.compat.filesystem.FileSystemFixer"));
        builder.setModuleConf(conf);
        return builder.createClientProfile();
    }

    public ClientProfile buildCleanRoom() {
        ClientProfileBuilder builder = new ClientProfileBuilder(profile);
        builder.setMainClass(forgeProfile.getMainClass());
        builder.setClassLoaderConfig(ClassLoaderConfig.LAUNCHER);

        List<String> clientArgs = new ArrayList<>();
        clientArgs.addAll(ClientToolkit.findValuesForKey(forgeProfile.getMinecraftArguments(), "tweakClass"));
        clientArgs.addAll(ClientToolkit.findValuesForKey(forgeProfile.getMinecraftArguments(), "versionType"));
        builder.setClientArgs(clientArgs);
        builder.setRecommendJavaVersion(21);
        builder.setMinJavaVersion(21);
        return builder.createClientProfile();
    }

    private String processPlaceholders(String value) {
        return value.replace("${library_directory}", "libraries");
    }

    private void processArg(String key, String value, LaunchOptions.ModuleConf conf) {
        if (key.equals("-p")) {
            String[] splited = value.split("\\$\\{classpath_separator}");
            conf.modulePath = new ArrayList<>(List.of(splited));
            return;
        }
        if (key.equals("--add-modules")) {
            String[] splited = value.split(",");
            conf.modules = new ArrayList<>(List.of(splited));
            return;
        }
        if (key.equals("--add-opens")) {
            String[] splited = value.split("=");
            if (conf.opens == null) {
                conf.opens = new HashMap<>();
            }
            conf.opens.put(splited[0], splited[1]);
            return;
        }
        if (key.equals("--add-exports")) {
            String[] splited = value.split("=");
            if (conf.exports == null) {
                conf.exports = new HashMap<>();
            }
            conf.exports.put(splited[0], splited[1]);
        }
    }
}
