package ru.ricardocraft.backend.base.profiles.optional.actions;

import java.util.List;

public class OptionalActionJvmArgs extends OptionalAction {

    public List<String> args;

    public OptionalActionJvmArgs(List<String> args) {
        this.args = args;
    }
}
