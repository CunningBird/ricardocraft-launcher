package ru.ricardocraft.backend.binary;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.binary.tasks.OSSLSignTask;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.io.IOException;
import java.nio.file.Files;

@Component
public class EXELauncherBinary extends LauncherBinary {

    @Autowired
    public EXELauncherBinary(LaunchServerConfig config, LaunchServerDirectories directories) {
        super(config, directories, LauncherBinary.resolve(config, ".exe"), "Launcher-%s.exe");
        tasks.add(new OSSLSignTask(this, config));
    }

    @PostConstruct
    public void check() throws IOException {
        logger.info("Syncing launcher EXE binary file");
        if (!sync()) logger.warn("Missing launcher EXE binary file");
    }

    @Override
    public void build() throws IOException {
        if (IOHelper.isFile(syncBinaryFile)) {
            Files.delete(syncBinaryFile);
        }
    }
}
