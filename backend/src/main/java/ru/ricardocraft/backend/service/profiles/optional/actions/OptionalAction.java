package ru.ricardocraft.backend.service.profiles.optional.actions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OptionalActionFile.class, name = "file"),
        @JsonSubTypes.Type(value = OptionalActionJvmArgs.class, name = "jvmArgs")
})
public abstract class OptionalAction {
}
