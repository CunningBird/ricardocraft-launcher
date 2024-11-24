package pro.gravit.launcher.gui.base.profiles.optional.actions;

import java.util.List;

public class OptionalActionJvmArgs extends OptionalAction {
    public List<String> args;

    public OptionalActionJvmArgs() {
    }

    public OptionalActionJvmArgs(List<String> args) {
        this.args = args;
    }
}
