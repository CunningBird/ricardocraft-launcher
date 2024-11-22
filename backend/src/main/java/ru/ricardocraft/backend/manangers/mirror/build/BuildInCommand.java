package ru.ricardocraft.backend.manangers.mirror.build;

import java.nio.file.Path;
import java.util.List;

public interface BuildInCommand {
    void run(List<String> args, BuildContext context, Path workdir) throws Exception;
}
