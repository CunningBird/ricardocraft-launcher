package ru.ricardocraft.backend.service.command.unsafe;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.base.helper.IOHelper;
import ru.ricardocraft.backend.service.patcher.UnsafePatcher;
import ru.ricardocraft.backend.service.DirectoriesService;
import ru.ricardocraft.backend.service.patcher.impl.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PatcherService {

    private final Map<String, UnsafePatcher> patchers = new HashMap<>();

    private final DirectoriesService directoriesService;

    public PatcherService(DirectoriesService directoriesService) {
        this.directoriesService = directoriesService;

        patchers.put("findSystem", new FindSystemPatcher());
        patchers.put("findRemote", new FindRemotePatcher());
        patchers.put("findSun", new FindSunPatcher());
        patchers.put("findPacketHack", new FindPacketHackPatcher());
        patchers.put("findDefineClass", new FindDefineClassPatcher());
        patchers.put("findReflect", new FindReflectPatcher());
    }

    @SuppressWarnings("unchecked")
    public void patcher(String name, String path, Boolean testMode, @Nullable String[] realArgs) throws Exception {
        Path target = Paths.get(path);
        UnsafePatcher patcher = patchers.get(name);
        if (patcher == null) {
            Class<? extends UnsafePatcher> clazz = (Class<? extends UnsafePatcher>) Class.forName(name);
            try {
                if (realArgs != null && realArgs.length > 0)
                    patcher = (UnsafePatcher) MethodHandles.publicLookup().findConstructor(clazz, MethodType.methodType(void.class, String[].class)).asFixedArity().invoke((Object) realArgs);
                else
                    patcher = (UnsafePatcher) MethodHandles.publicLookup().findConstructor(clazz, MethodType.methodType(void.class)).invoke();
            } catch (Throwable e) {
                log.debug(e.getMessage());
                try {
                    patcher = (UnsafePatcher) MethodHandles.publicLookup().findConstructor(clazz, MethodType.methodType(void.class)).invokeWithArguments();
                } catch (Throwable t) {
                    throw (InstantiationException) new InstantiationException().initCause(t);
                }
            }
        }
        if (!IOHelper.exists(target))
            throw new IllegalStateException("Target path not exist");
        Path tempFile = directoriesService.getBuildDir().resolve("patcher.tmp.jar");
        if (IOHelper.isFile(target)) {
            patcher.processFile(target, tempFile, testMode);
        } else if (IOHelper.isDir(target)) {
            patcher.processDir(target, tempFile, testMode);
        }
    }
}
