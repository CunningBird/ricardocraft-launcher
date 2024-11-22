package ru.ricardocraft.backend.binary.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ProGuardMultiReleaseFixer implements LauncherBuildTask {

    private transient final Logger logger = LogManager.getLogger(ProGuardMultiReleaseFixer.class);

    private final LaunchServerProperties properties;
    private final JarLauncherBinary launcherBinary;
    private final String proguardTaskName;

    public ProGuardMultiReleaseFixer(JarLauncherBinary launcherBinary,
                                     LaunchServerProperties properties,
                                     String proguardTaskName) {
        this.properties = properties;
        this.launcherBinary = launcherBinary;
        this.proguardTaskName = proguardTaskName;
    }

    @Override
    public String getName() {
        return "ProGuardMultiReleaseFixer.proguard";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        if (!properties.getProguard().getEnabled()) {
            return inputFile;
        }
        LauncherBuildTask task = launcherBinary.getTaskBefore((x) -> proguardTaskName.equals(x.getName())).get();
        Path lastPath = launcherBinary.nextPath(task);
        if(Files.notExists(lastPath)) {
            logger.error("{} not exist. Multi-Release JAR fix not applied!", lastPath);
            return inputFile;
        }
        Path outputPath = launcherBinary.nextPath(this);
        try(ZipOutputStream output = new ZipOutputStream(new FileOutputStream(outputPath.toFile()))) {
            try(ZipInputStream input = new ZipInputStream(new FileInputStream(inputFile.toFile()))) {
                ZipEntry entry = input.getNextEntry();
                while(entry != null) {
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    output.putNextEntry(newEntry);
                    input.transferTo(output);
                    entry = input.getNextEntry();
                }
            }
            try(ZipInputStream input = new ZipInputStream(new FileInputStream(lastPath.toFile()))) {
                ZipEntry entry = input.getNextEntry();
                while(entry != null) {
                    if(!entry.getName().startsWith("META-INF/versions")) {
                        entry = input.getNextEntry();
                        continue;
                    }
                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    output.putNextEntry(newEntry);
                    input.transferTo(output);
                    entry = input.getNextEntry();
                }
            }
        }
        return outputPath;
    }
}
