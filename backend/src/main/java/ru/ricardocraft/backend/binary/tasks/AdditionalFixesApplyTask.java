package ru.ricardocraft.backend.binary.tasks;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import ru.ricardocraft.backend.LaunchServer;
import ru.ricardocraft.backend.asm.ClassMetadataReader;
import ru.ricardocraft.backend.asm.SafeClassWriter;
import ru.ricardocraft.backend.binary.JARLauncherBinary;
import ru.ricardocraft.backend.helper.IOHelper;
import ru.ricardocraft.backend.properties.LaunchServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AdditionalFixesApplyTask implements LauncherBuildTask {
    private final JARLauncherBinary launcherBinary;
    private final LaunchServerConfig config;

    public AdditionalFixesApplyTask(JARLauncherBinary launcherBinary, LaunchServerConfig config) {
        this.launcherBinary = launcherBinary;
        this.config = config;
    }

    public static void apply(Path inputFile, Path addFile, ZipOutputStream output, LaunchServerConfig config, Predicate<ZipEntry> excluder, boolean needFixes) throws IOException {
        try (ClassMetadataReader reader = new ClassMetadataReader()) {
            reader.getCp().add(new JarFile(inputFile.toFile()));
            try (ZipInputStream input = IOHelper.newZipInput(addFile)) {
                ZipEntry e = input.getNextEntry();
                while (e != null) {
                    if (e.isDirectory() || excluder.test(e)) {
                        e = input.getNextEntry();
                        continue;
                    }
                    String filename = e.getName();
                    output.putNextEntry(IOHelper.newZipEntry(e));
                    if (filename.endsWith(".class")) {
                        byte[] bytes;
                        if (needFixes) {
                            bytes = classFix(input, reader, config.launcher.stripLineNumbers);
                            output.write(bytes);
                        } else
                            IOHelper.transfer(input, output);
                    } else
                        IOHelper.transfer(input, output);
                    e = input.getNextEntry();
                }
            }
        }
    }

    private static byte[] classFix(InputStream input, ClassMetadataReader reader, boolean stripNumbers) throws IOException {
        ClassReader cr = new ClassReader(input);
        ClassNode cn = new ClassNode();
        cr.accept(cn, stripNumbers ? (ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES) : ClassReader.SKIP_FRAMES);
        ClassWriter cw = new SafeClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
    }

    @Override
    public String getName() {
        return "AdditionalFixesApply";
    }

    @Override
    public Path process(Path inputFile) throws IOException {
        Path out = launcherBinary.nextPath("post-fixed");
        try (ZipOutputStream output = new ZipOutputStream(IOHelper.newOutput(out))) {
            apply(inputFile, inputFile, output, config, (e) -> false, true);
        }
        return out;
    }

}
