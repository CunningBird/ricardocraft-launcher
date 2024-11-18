package ru.ricardocraft.backend.command.unsafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.command.Command;
import ru.ricardocraft.backend.command.unsafe.patcher.UnsafePatcher;
import ru.ricardocraft.backend.command.unsafe.patcher.impl.*;
import ru.ricardocraft.backend.properties.LaunchServerDirectories;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class PatcherCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(PatcherCommand.class);

    public static Map<String, UnsafePatcher> patchers = new HashMap<>();

    private transient final LaunchServerDirectories directories;

    @Autowired
    public PatcherCommand(LaunchServerDirectories directories) {
        super();
        this.directories = directories;
    }

    @Override
    public String getArgsDescription() {
        return "[patcher name or class] [path] [test mode(true/false)] (other args)";
    }

    @Override
    public String getUsageDescription() {
        return "";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(String... args) throws Exception {
        if (patchers.isEmpty()) {
            patchers.put("findSystem", new FindSystemPatcher());
            patchers.put("findRemote", new FindRemotePatcher());
            patchers.put("findSun", new FindSunPatcher());
            patchers.put("findPacketHack", new FindPacketHackPatcher());
            patchers.put("findDefineClass", new FindDefineClassPatcher());
            patchers.put("findReflect", new FindReflectPatcher());
        }
        verifyArgs(args, 3);
        String name = args[0];
        Path target = Paths.get(args[1]);
        boolean testMode = Boolean.parseBoolean(args[2]);
        UnsafePatcher patcher = patchers.get(name);
        if (patcher == null) {
            Class<? extends UnsafePatcher> clazz = (Class<? extends UnsafePatcher>) Class.forName(name);
            try {
                String[] real_args = Arrays.copyOfRange(args, 3, args.length);
                if (real_args.length > 0)
                    patcher = (UnsafePatcher) MethodHandles.publicLookup().findConstructor(clazz, MethodType.methodType(void.class, String[].class)).asFixedArity().invoke(real_args);
                else
                    patcher = (UnsafePatcher) MethodHandles.publicLookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            } catch (Throwable e) {
                logger.debug(e.getMessage());
                try {
                    patcher = (UnsafePatcher) MethodHandles.publicLookup().findConstructor(clazz, MethodType.methodType(void.class)).invokeWithArguments();
                } catch (Throwable t) {
                    throw (InstantiationException) new InstantiationException().initCause(t);
                }
            }
        }
        if (!IOHelper.exists(target))
            throw new IllegalStateException("Target path not exist");
        Path tempFile = directories.dir.resolve("build").resolve("patcher.tmp.jar");
        if (IOHelper.isFile(target)) {
            patcher.processFile(target, tempFile, testMode);
        } else if (IOHelper.isDir(target)) {
            patcher.processDir(target, tempFile, testMode);
        }
    }
}
