package ru.ricardocraft.backend.base.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class UnpackHelper {
    public static void unpack(URL resource, Path target) throws IOException {
        if (IOHelper.isFile(target)) {
            if (matches(target, resource)) return;
        }
        Files.deleteIfExists(target);
        IOHelper.createParentDirs(target);
        try (InputStream in = IOHelper.newInput(resource)) {
            IOHelper.transfer(in, target);
        }
    }

    private static boolean matches(Path target, URL in) {
        try {
            return Arrays.equals(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, in),
                    SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, target));
        } catch (IOException e) {
            return false;
        }
    }
}
