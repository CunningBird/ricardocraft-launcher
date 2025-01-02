package ru.ricardocraft.client.base.utils.launch;

import ru.ricardocraft.client.service.profiles.ClientProfile;
import ru.ricardocraft.client.service.client.ClientParams;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface Launch {
    ClassLoaderControl init(List<Path> files, String nativePath, LaunchOptions options);
    ProcessBuilder getLaunchProcess(List<String> processArgs, ClientParams params, ClientProfile profile) throws Throwable;
    void launch(String mainClass, String mainModule, Collection<String> args) throws Throwable;
}
