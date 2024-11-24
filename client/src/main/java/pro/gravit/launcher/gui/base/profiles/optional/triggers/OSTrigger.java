package pro.gravit.launcher.gui.base.profiles.optional.triggers;

import pro.gravit.launcher.gui.base.profiles.optional.OptionalFile;
import pro.gravit.launcher.gui.utils.helper.JVMHelper;

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
