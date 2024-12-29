package ru.ricardocraft.backend.service.profiles.optional.actions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class OptionalActionFile extends OptionalAction {

    public Map<String, String> files;

    public OptionalActionFile(Map<String, String> files) {
        this.files = files;
    }
}
