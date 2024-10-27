package pro.gravit.launcher.gui.base.profiles.optional.actions;

import pro.gravit.launcher.gui.base.profiles.optional.actions.OptionalAction;

import java.util.List;

public class OptionalActionClassPath extends OptionalAction {
    public List<String> args;
    public boolean useAltClasspath = false;

    public OptionalActionClassPath() {
    }

    public OptionalActionClassPath(List<String> args) {
        this.args = args;
    }

    public OptionalActionClassPath(List<String> args, boolean useAltClasspath) {
        this.args = args;
        this.useAltClasspath = useAltClasspath;
    }
}
