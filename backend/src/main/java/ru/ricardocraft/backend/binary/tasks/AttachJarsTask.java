package ru.ricardocraft.backend.binary.tasks;

import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.binary.JarLauncherBinary;
import ru.ricardocraft.backend.binary.JarLauncherInfo;
import ru.ricardocraft.backend.properties.LaunchServerProperties;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AttachJarsTask implements LauncherBuildTask {

    private final JarLauncherInfo jarLauncherInfo;
    private final JarLauncherBinary launcherBinary;
    private final LaunchServerProperties config;
    private final List<Path> jars;
    private final List<String> exclusions;

    public AttachJarsTask(JarLauncherInfo jarLauncherInfo,
                          JarLauncherBinary launcherBinary,
                          LaunchServerProperties config) {
        this.jarLauncherInfo = jarLauncherInfo;
        this.launcherBinary = launcherBinary;
        this.config = config;

        jars = new ArrayList<>();
        exclusions = new ArrayList<>();
        exclusions.add("META-INF");
        exclusions.add("module-info.class");
        exclusions.add("LICENSE");
        exclusions.add("LICENSE.txt");
    }

    @Override
    public String getName() {
        return "AttachJars";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        Path outputFile = launcherBinary.nextPath("attached");
        try (ZipInputStream input = IOHelper.newZipInput(inputFile); ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(outputFile))) {
            ZipEntry e = input.getNextEntry();
            while (e != null) {
                if (e.isDirectory()) {
                    e = input.getNextEntry();
                    continue;
                }
                output.putNextEntry(IOHelper.newZipEntry(e));
                IOHelper.transfer(input, output);
                e = input.getNextEntry();
            }
            attach(output, inputFile, jarLauncherInfo.getCoreLibs());
            attach(output, inputFile, jars);
            for (var entry : jarLauncherInfo.getFiles().entrySet()) {
                ZipEntry newEntry = IOHelper.newZipEntry(entry.getKey());
                output.putNextEntry(newEntry);
                IOHelper.transfer(entry.getValue(), output);
            }
        }
        return outputFile;
    }

    private void attach(ZipOutputStream output, Path inputFile, List<Path> lst) throws IOException {
        for (Path p : lst) {
            AdditionalFixesApplyTask.apply(inputFile, p, output, config, (e) -> filter(e.getName()), false);
        }
    }

    private boolean filter(String name) {
        if (name.startsWith("META-INF/services")) return false;
        return exclusions.stream().anyMatch(name::startsWith);
    }
}
