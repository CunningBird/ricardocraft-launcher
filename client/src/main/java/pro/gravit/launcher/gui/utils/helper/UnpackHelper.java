package pro.gravit.launcher.gui.utils.helper;

import pro.gravit.launcher.gui.utils.helper.IOHelper;
import pro.gravit.launcher.gui.utils.helper.SecurityHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class UnpackHelper {
    public static void unpack(URL resource, Path target) throws IOException {
        if (pro.gravit.launcher.gui.utils.helper.IOHelper.isFile(target)) {
            if (matches(target, resource)) return;
        }
        Files.deleteIfExists(target);
        pro.gravit.launcher.gui.utils.helper.IOHelper.createParentDirs(target);
        try (InputStream in = pro.gravit.launcher.gui.utils.helper.IOHelper.newInput(resource)) {
            pro.gravit.launcher.gui.utils.helper.IOHelper.transfer(in, target);
        }
    }

    private static boolean matches(Path target, URL in) {
        try {
            return Arrays.equals(pro.gravit.launcher.gui.utils.helper.SecurityHelper.digest(pro.gravit.launcher.gui.utils.helper.SecurityHelper.DigestAlgorithm.SHA256, in),
                    pro.gravit.launcher.gui.utils.helper.SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, target));
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean unpackZipNoCheck(URL resource, Path target) throws IOException {
        if (Files.isDirectory(target))
            return false;
        Files.deleteIfExists(target);
        Files.createDirectory(target);
        try (ZipInputStream input = pro.gravit.launcher.gui.utils.helper.IOHelper.newZipInput(resource)) {
            for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                if (entry.isDirectory())
                    continue; // Skip dirs
                // Unpack file
                pro.gravit.launcher.gui.utils.helper.IOHelper.transfer(input, target.resolve(pro.gravit.launcher.gui.utils.helper.IOHelper.toPath(entry.getName())));
            }
        }
        return true;
    }

    public static void unpackZipNoCheck(String resource, Path target) throws IOException {
        try {
            if (Files.isDirectory(target))
                return;
            Files.deleteIfExists(target);
            Files.createDirectory(target);
            try (ZipInputStream input = pro.gravit.launcher.gui.utils.helper.IOHelper.newZipInput(pro.gravit.launcher.gui.utils.helper.IOHelper.getResourceURL(resource))) {
                for (ZipEntry entry = input.getNextEntry(); entry != null; entry = input.getNextEntry()) {
                    if (entry.isDirectory())
                        continue; // Skip dirs
                    // Unpack file
                    pro.gravit.launcher.gui.utils.helper.IOHelper.transfer(input, target.resolve(IOHelper.toPath(entry.getName())));
                }
            }
        } catch (NoSuchFileException ignored) {
        }
    }
}
