package ru.ricardocraft.client.helper.enfs.dir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

public class CachedFile extends FileEntry {
    private final FileEntry delegate;
    private volatile SoftReference<byte[]> cache;

    public CachedFile(FileEntry delegate) {
        this.delegate = delegate;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        var cachedBytes = cache == null ? null : cache.get();
        if(cachedBytes != null) {
            return new ByteArrayInputStream(cachedBytes);
        }
        var cached = tryCache();
        if(cached != null) {
            return cached;
        }
        return delegate.getInputStream();
    }

    private synchronized InputStream tryCache() throws IOException {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try(InputStream input = delegate.getInputStream()) {
                input.transferTo(output);
            }
            byte[] bytes = output.toByteArray();
            cache = new SoftReference<>(bytes);
            return new ByteArrayInputStream(bytes);
        } catch (OutOfMemoryError ignored) {
        }
        return null;
    }

}
