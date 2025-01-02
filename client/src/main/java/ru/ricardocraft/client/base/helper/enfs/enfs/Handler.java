package ru.ricardocraft.client.base.helper.enfs.enfs;

import ru.ricardocraft.client.base.helper.EnFSHelper;
import ru.ricardocraft.client.base.helper.enfs.EnFS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class Handler extends URLStreamHandler {

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        EnFS enFS = EnFSHelper.getEnFS();
        String realPath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8);
        var fileEntry = enFS.getFile(Paths.get(realPath));
        if (fileEntry == null) throw new FileNotFoundException(url.toString());
        return fileEntry.openConnection(url);
    }
}
