package pro.gravit.launcher.gui.base.profiles.optional.actions;

import pro.gravit.launcher.gui.base.profiles.optional.actions.OptionalAction;

import java.util.List;

public class OptionalActionClientArgs extends OptionalAction {
    public List<String> args;

    public OptionalActionClientArgs() {
    }

    public OptionalActionClientArgs(List<String> args) {
        this.args = args;
    }
}
