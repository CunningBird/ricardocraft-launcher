package ru.ricardocraft.client.commands;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ricardocraft.client.base.helper.LogHelper;

import java.security.interfaces.ECPublicKey;
import java.util.Base64;

@Component
public class GetPublicKeyCommand extends Command {

    private final ECPublicKey publicKey;

    @Autowired
    public GetPublicKeyCommand(CommandHandler commandHandler, ECPublicKey publicKey) {
        this.publicKey = publicKey;
        commandHandler.registerCommand("getpublickey", this);
    }

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
        LogHelper.info("PublicKey: %s", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
    }
}
