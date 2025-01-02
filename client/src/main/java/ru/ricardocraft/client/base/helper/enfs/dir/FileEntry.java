package ru.ricardocraft.client.base.helper.enfs.dir;

import ru.ricardocraft.client.base.helper.enfs.enfs.EnFSURLConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class FileEntry {
    public abstract InputStream getInputStream() throws IOException;

    public URLConnection openConnection(URL url) throws IOException {
        return new EnFSURLConnection(url, this);
    }
}
