package ru.ricardocraft.client.utils.launch;

import ru.ricardocraft.client.profiles.ClientProfile;
import ru.ricardocraft.client.client.ClientParams;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface Launch {
    ClassLoaderControl init(List<Path> files, String nativePath, LaunchOptions options);
    ProcessBuilder getLaunchProcess(List<String> processArgs, ClientParams params, ClientProfile profile) throws Throwable;
    void launch(String mainClass, String mainModule, Collection<String> args) throws Throwable;
}
