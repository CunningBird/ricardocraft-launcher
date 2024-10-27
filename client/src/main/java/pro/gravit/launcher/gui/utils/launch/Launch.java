package pro.gravit.launcher.gui.utils.launch;

import pro.gravit.launcher.gui.utils.launch.ClassLoaderControl;
import pro.gravit.launcher.gui.utils.launch.LaunchOptions;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface Launch {
    ClassLoaderControl init(List<Path> files, String nativePath, LaunchOptions options);
    void launch(String mainClass, String mainModule, Collection<String> args) throws Throwable;
}
