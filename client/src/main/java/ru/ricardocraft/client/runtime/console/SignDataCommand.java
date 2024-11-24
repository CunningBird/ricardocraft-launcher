package ru.ricardocraft.client.runtime.console;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.util.Base64;

public class SignDataCommand extends Command {

    @Override
    public String getArgsDescription() {
        return "[base64 data]";
    }

    @Override
    public String getUsageDescription() {
        return "sign any data";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        byte[] data = Base64.getDecoder().decode(args[0]);
        byte[] signature = JavaFXApplication.sign(data);
        String base64 = Base64.getEncoder().encodeToString(signature);
        LogHelper.info("Signature: %s", base64);
    }
}
