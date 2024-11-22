package ru.ricardocraft.backend.manangers.mirror.build;

import ru.ricardocraft.backend.base.helper.IOHelper;

import java.nio.file.Path;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class FetchManifestValue implements BuildInCommand {

    @Override
    public void run(List<String> args, BuildContext context, Path workdir) throws Exception {
        Path filePath = context.targetClientDir.resolve(args.get(0));
        String[] splited = args.get(1).split(",");
        String varName = args.get(2);
        try (JarInputStream input = new JarInputStream(IOHelper.newInput(filePath))) {
            Manifest manifest = input.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            for (var e : splited) {
                var value = attributes.getValue(e);
                if (value != null) {
                    context.variables.put(varName, value);
                    return;
                }
                for (var entity : manifest.getEntries().entrySet()) {
                    value = entity.getValue().getValue(e);
                    if (value != null) {
                        context.variables.put(varName, value);
                        return;
                    }
                }
            }
            throw new RuntimeException(String.format("Manifest values %s not found in %s", args.get(1), filePath));
        }
    }
}