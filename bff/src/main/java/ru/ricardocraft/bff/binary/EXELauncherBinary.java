package ru.ricardocraft.bff.binary;

import ru.ricardocraft.bff.LaunchServer;
import ru.ricardocraft.bff.binary.tasks.OSSLSignTask;
import ru.ricardocraft.bff.helper.IOHelper;

import java.io.IOException;
import java.nio.file.Files;

public class EXELauncherBinary extends LauncherBinary {

    public EXELauncherBinary(LaunchServer server) {
        super(server, LauncherBinary.resolve(server, ".exe"), "Launcher-%s.exe");
    }

    @Override
    public void build() throws IOException {
        if (IOHelper.isFile(syncBinaryFile)) {
            Files.delete(syncBinaryFile);
        }
    }

    @Override
    public void init() {
        tasks.add(new OSSLSignTask(server));
    }
}
