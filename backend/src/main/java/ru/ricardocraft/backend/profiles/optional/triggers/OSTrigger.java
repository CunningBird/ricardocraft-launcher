package ru.ricardocraft.backend.profiles.optional.triggers;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ricardocraft.backend.base.helper.JVMHelper;
import ru.ricardocraft.backend.profiles.optional.OptionalFile;

@NoArgsConstructor
@Getter
@Setter
public class OSTrigger extends OptionalTrigger {
    public JVMHelper.OS os;

    public OSTrigger(JVMHelper.OS os) {
        this.os = os;
    }

    @Override
    public boolean isTriggered(OptionalFile optional, OptionalTriggerContext context) {
        return JVMHelper.OS_TYPE == os;
    }
}
