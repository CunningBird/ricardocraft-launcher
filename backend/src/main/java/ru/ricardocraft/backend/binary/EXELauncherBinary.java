package ru.ricardocraft.backend.binary;

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

    private final LaunchServerConfig config;

    @Autowired
    public EXELauncherBinary(LaunchServerConfig config, LaunchServerDirectories directories) {
        super(config, directories, LauncherBinary.resolve(config, ".exe"), "Launcher-%s.exe");
        this.config = config;
    }

    @Override
    public void build() throws IOException {
        if (IOHelper.isFile(syncBinaryFile)) {
            Files.delete(syncBinaryFile);
        }
        tasks.add(new OSSLSignTask(this, config));
    }
}
