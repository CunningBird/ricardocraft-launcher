package ru.ricardocraft.backend.manangers.mirror.build;

import ru.ricardocraft.backend.base.helper.IOHelper;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public class UpdateGradle implements BuildInCommand {

    @Override
    public void run(List<String> args, BuildContext context, Path workdir) throws Exception {
        var repoDir = args.get(0);
        var toVersion = args.get(1);
        var propertiesPath = Path.of(repoDir).resolve("gradle").resolve("wrapper").resolve("gradle-wrapper.properties");
        Properties properties = new Properties();
        try (var input = IOHelper.newInput(propertiesPath)) {
            properties.load(input);
        }
        properties.put("distributionUrl", "https://services.gradle.org/distributions/gradle-" + toVersion + "-bin.zip");
        try (var output = IOHelper.newOutput(propertiesPath)) {
            properties.store(output, null);
        }
    }
}