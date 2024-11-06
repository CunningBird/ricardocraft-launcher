package ru.ricardocraft.backend.base.profiles.optional.actions;

import java.util.List;

public class OptionalActionClientArgs extends OptionalAction {
    public List<String> args;

    public OptionalActionClientArgs() {
    }

    public OptionalActionClientArgs(List<String> args) {
        this.args = args;
    }
}
