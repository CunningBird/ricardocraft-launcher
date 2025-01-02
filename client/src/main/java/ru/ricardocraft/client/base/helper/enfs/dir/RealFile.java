package ru.ricardocraft.client.base.helper.enfs.dir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class RealFile extends FileEntry {
    private final Path file;

    public RealFile(Path file) {
        this.file = file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(file);
    }

    @Override
    public URLConnection openConnection(URL url) {
        try {
            return file.toUri().toURL().openConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
