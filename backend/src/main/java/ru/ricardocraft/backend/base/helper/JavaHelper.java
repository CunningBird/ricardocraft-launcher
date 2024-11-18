package ru.ricardocraft.backend.base.helper;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JavaHelper {
    public static final List<String> javaFxModules = List.of("javafx.base", "javafx.graphics", "javafx.fxml", "javafx.controls", "javafx.swing", "javafx.media", "javafx.web");

    public static class JavaVersion {
        public final Path jvmDir;
        public final int version;
        public final int build;
        public final JVMHelper.ARCH arch;
        public final List<String> modules;
        public boolean enabledJavaFX;

        public JavaVersion(Path jvmDir, int version, int build, JVMHelper.ARCH arch, boolean enabledJavaFX) {
            this.jvmDir = jvmDir;
            this.version = version;
            this.build = build;
            this.arch = arch;
            this.enabledJavaFX = enabledJavaFX;
            if(version > 8) {
                this.modules = javaFxModules;
            } else {
                this.modules = Collections.unmodifiableList(new ArrayList<>());
            }
        }
    }
}
