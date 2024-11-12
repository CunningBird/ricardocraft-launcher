package ru.ricardocraft.backend.manangers;

import org.springframework.stereotype.Component;
import ru.ricardocraft.backend.Reconfigurable;
import ru.ricardocraft.backend.command.utls.Command;
import ru.ricardocraft.backend.helper.VerifyHelper;

import java.util.HashMap;
import java.util.Map;

@Component
public class ReconfigurableManager {
    private final HashMap<String, Command> RECONFIGURABLE = new HashMap<>();

    public void registerReconfigurable(String name, Reconfigurable reconfigurable) {
        VerifyHelper.putIfAbsent(RECONFIGURABLE, name.toLowerCase(), new ReconfigurableVirtualCommand(reconfigurable.getCommands()),
                "Reconfigurable has been already registered: '%s'".formatted(name));
    }

    public void unregisterReconfigurable(String name) {
        RECONFIGURABLE.remove(name.toLowerCase());
    }

    public Map<String, Command> getCommands() {
        return RECONFIGURABLE;
    }

    public void registerObject(String name, Object object) {
        if (object instanceof Reconfigurable) {
            registerReconfigurable(name, (Reconfigurable) object);
        }
    }

    public void unregisterObject(String name, Object object) {
        if (object instanceof Reconfigurable) {
            unregisterReconfigurable(name);
        }
    }

    private static class ReconfigurableVirtualCommand extends Command {
        public ReconfigurableVirtualCommand(Map<String, Command> childs) {
            super(childs);
        }

        @Override
        public String getArgsDescription() {
            return null;
        }

        @Override
        public String getUsageDescription() {
            return null;
        }

        @Override
        public void invoke(String... args) throws Exception {
            invokeSubcommands(args);
        }
    }
}
