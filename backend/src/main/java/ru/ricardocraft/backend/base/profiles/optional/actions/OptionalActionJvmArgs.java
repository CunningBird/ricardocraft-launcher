package ru.ricardocraft.backend.base.profiles.optional.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class OptionalActionJvmArgs extends OptionalAction {

    public List<String> args;

    public OptionalActionJvmArgs(List<String> args) {
        this.args = args;
    }
}
