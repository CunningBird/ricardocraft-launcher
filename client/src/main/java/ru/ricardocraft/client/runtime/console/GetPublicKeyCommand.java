package ru.ricardocraft.client.runtime.console;

import ru.ricardocraft.client.JavaFXApplication;
import ru.ricardocraft.client.utils.command.Command;
import ru.ricardocraft.client.utils.helper.LogHelper;

import java.util.Base64;

public class GetPublicKeyCommand extends Command {

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "print public key in base64 format";
    }

    @Override
    public void invoke(String... args) {
        LogHelper.info("PublicKey: %s", Base64.getEncoder().encodeToString(JavaFXApplication.getClientPublicKey().getEncoded()));
    }
}
