package ru.ricardocraft.backend.binary;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.auth.updates.UpdatesProvider;
import ru.ricardocraft.backend.binary.tasks.*;
import ru.ricardocraft.backend.manangers.CertificateManager;
import ru.ricardocraft.backend.manangers.DirectoriesManager;
import ru.ricardocraft.backend.manangers.KeyAgreementManager;
import ru.ricardocraft.backend.properties.DirectoriesProperties;
import ru.ricardocraft.backend.properties.LaunchServerProperties;
import ru.ricardocraft.backend.properties.NettyProperties;
import ru.ricardocraft.backend.properties.config.ProguardConfig;

import java.io.IOException;

@Component
public final class JarLauncherBinary extends LauncherBinary {

    @Autowired
    public JarLauncherBinary(JarLauncherInfo jarLauncherInfo,
                             LaunchServerProperties properties,
                             DirectoriesProperties directoriesProperties,
                             DirectoriesManager directoriesManager,
                             NettyProperties nettyProperties,
                             KeyAgreementManager keyAgreementManager,
                             CertificateManager certificateManager,
                             UpdatesProvider updatesProvider,
                             ProguardConfig proguardConfig) throws IOException {

        super(directoriesManager, updatesProvider, resolve(properties, ".jar"), "Launcher-%s.jar");

        tasks.add(new PrepareBuildTask(jarLauncherInfo, directoriesManager));
        if (!properties.getSign().getEnabled()) tasks.add(new CertificateAutogenTask(properties, keyAgreementManager));
        tasks.add(new MainBuildTask(jarLauncherInfo, this, properties, directoriesProperties, directoriesManager, nettyProperties, keyAgreementManager, certificateManager));
        tasks.add(new ProGuardBuildTask(jarLauncherInfo, this, properties, directoriesManager, proguardConfig));
        tasks.add(new ProGuardMultiReleaseFixer(this, properties, "ProGuard.proguard"));
        tasks.add(new AttachJarsTask(jarLauncherInfo, this, properties));
        tasks.add(new AdditionalFixesApplyTask(this, properties));
        if (properties.getLauncher().getCompress()) tasks.add(new CompressBuildTask(this));
        tasks.add(new SignJarTask(this, properties.getSign()));
    }

    @PostConstruct
    public void check() throws IOException {
        logger.info("Syncing launcher binary file");
        if (!sync()) logger.warn("Missing launcher binary file");
    }
}
