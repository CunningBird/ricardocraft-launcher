package ru.ricardocraft.backend.binary;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.base.Launcher;
import ru.ricardocraft.backend.binary.tasks.*;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public final class JARLauncherBinary extends LauncherBinary {

    public final AtomicLong count;
    public final Path runtimeDir;
    public final Path buildDirectory;
    public final List<Path> coreLibs;
    public final List<Path> addonLibs;

    public final Map<String, Path> files;

    @Autowired
    public JARLauncherBinary(LaunchServerConfig config,
                             LaunchServerProperties properties,
                             LaunchServerDirectories directories,
                             KeyAgreementManager keyAgreementManager,
                             CertificateManager certificateManager,
                             UpdatesProvider updatesProvider) throws IOException {
        super(directories, updatesProvider, resolve(properties, ".jar"), "Launcher-%s.jar");

        count = new AtomicLong(0);
        runtimeDir = directories.dir.resolve(Launcher.RUNTIME_DIR);
        buildDirectory = directories.dir.resolve("build");
        coreLibs = new ArrayList<>();
        addonLibs = new ArrayList<>();
        files = new HashMap<>();
        if (!Files.isDirectory(buildDirectory)) {
            Files.deleteIfExists(buildDirectory);
            Files.createDirectory(buildDirectory);
        }

        tasks.add(new PrepareBuildTask(this, directories));
        if (!config.sign.enabled) tasks.add(new CertificateAutogenTask(config, keyAgreementManager));
        tasks.add(new MainBuildTask(this, config, keyAgreementManager, certificateManager));
        tasks.add(new AttachJarsTask(this, config));
        tasks.add(new AdditionalFixesApplyTask(this, config));
        if (config.launcher.compress) tasks.add(new CompressBuildTask(this));
        tasks.add(new SignJarTask(this, config.sign));
    }

    @PostConstruct
    public void check() throws IOException {
        logger.info("Syncing launcher binary file");
        if (!sync()) logger.warn("Missing launcher binary file");
    }
}
