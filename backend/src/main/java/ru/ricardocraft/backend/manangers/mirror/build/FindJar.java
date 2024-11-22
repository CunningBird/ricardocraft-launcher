package ru.ricardocraft.backend.manangers.mirror.build;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class FindJar implements BuildInCommand {

    @Override
    public void run(List<String> args, BuildContext context, Path workdir) throws Exception {
        Path filePath = context.targetClientDir.resolve(args.get(0));
        String varName = args.get(1);
        if (Files.notExists(filePath)) {
            throw new FileNotFoundException(filePath.toAbsolutePath().toString());
        }
        if (Files.isDirectory(filePath)) {
            try (Stream<Path> stream = Files.walk(filePath)) {
                filePath = stream.filter(e -> !Files.isDirectory(e) && e.getFileName().toString().endsWith(".jar")).findFirst().orElseThrow();
            }
        }
        context.variables.put(varName, filePath.toAbsolutePath().toString());
        if (args.size() >= 3) {
            var version = filePath.getParent().getFileName().toString();
            context.variables.put(args.get(2), version);
        }
    }
}