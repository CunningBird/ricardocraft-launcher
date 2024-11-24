package ru.ricardocraft.client.helper.enfs.dir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class URLFile extends FileEntry {
    private final URL url;

    public URLFile(URL url) {
        this.url = url;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return url.openConnection().getInputStream();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return this.url.openConnection();
    }
}
