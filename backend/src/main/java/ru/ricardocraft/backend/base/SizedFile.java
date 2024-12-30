package ru.ricardocraft.backend.base;

public class SizedFile {
    public final String urlPath, filePath;
    public final long size;

    public SizedFile(String urlPath, String filePath, long size) {
        this.urlPath = urlPath;
        this.filePath = filePath;
        this.size = size;
    }
}
