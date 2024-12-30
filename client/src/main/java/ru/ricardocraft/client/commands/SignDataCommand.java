package ru.ricardocraft.client.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.helper.LogHelper;
import ru.ricardocraft.client.helper.SecurityHelper;

import java.security.interfaces.ECPrivateKey;
import java.util.Base64;

@Component
public class SignDataCommand extends Command {

    private final ECPrivateKey privateKey;

    @Autowired
    public SignDataCommand(ECPrivateKey privateKey, CommandHandler commandHandler) {
        this.privateKey = privateKey;
        commandHandler.registerCommand("signdata", this);
    }

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
        byte[] signature = SecurityHelper.sign(data, privateKey);
        String base64 = Base64.getEncoder().encodeToString(signature);
        LogHelper.info("Signature: %s", base64);
    }
}
