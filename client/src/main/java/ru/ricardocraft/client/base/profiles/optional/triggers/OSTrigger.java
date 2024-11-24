package ru.ricardocraft.client.base.profiles.optional.triggers;

import ru.ricardocraft.client.base.profiles.optional.OptionalFile;
import ru.ricardocraft.client.utils.helper.JVMHelper;

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
