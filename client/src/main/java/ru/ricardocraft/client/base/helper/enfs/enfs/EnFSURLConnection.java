package ru.ricardocraft.client.base.helper.enfs.enfs;

import ru.ricardocraft.client.base.helper.enfs.dir.FileEntry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class EnFSURLConnection extends URLConnection {
    private final FileEntry fileEntry;
    public EnFSURLConnection(URL url, FileEntry fileEntry) {
        super(url);
        this.fileEntry = fileEntry;
    }

    @Override
    public void connect() {
        // NOP
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileEntry.getInputStream();
    }
}
