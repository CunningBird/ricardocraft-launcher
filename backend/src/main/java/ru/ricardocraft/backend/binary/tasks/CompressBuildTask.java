package ru.ricardocraft.backend.binary.tasks;

import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.base.helper.IOHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressBuildTask implements LauncherBuildTask {
    private final transient JARLauncherBinary launcherBinary;

    public CompressBuildTask(JARLauncherBinary launcherBinary) {
        this.launcherBinary = launcherBinary;
    }

    @Override
    public String getName() {
        return "compress";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        Path output = launcherBinary.nextPath(this);
        try (ZipOutputStream outputStream = new ZipOutputStream(IOHelper.newOutput(output))) {
            outputStream.setMethod(ZipOutputStream.DEFLATED);
            outputStream.setLevel(Deflater.BEST_COMPRESSION);
            try (ZipInputStream input = IOHelper.newZipInput(inputFile)) {
                ZipEntry e = input.getNextEntry();
                while (e != null) {
                    if (e.isDirectory()) {
                        e = input.getNextEntry();
                        continue;
                    }
                    outputStream.putNextEntry(IOHelper.newZipEntry(e));
                    IOHelper.transfer(input, outputStream);
                    e = input.getNextEntry();
                }
            }
        }
        return output;
    }
}
