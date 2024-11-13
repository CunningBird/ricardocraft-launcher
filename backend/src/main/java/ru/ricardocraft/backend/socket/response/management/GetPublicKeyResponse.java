package ru.ricardocraft.backend.socket.response.management;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.backend.base.events.request.GetPublicKeyRequestEvent;
import ru.ricardocraft.backend.socket.Client;
import ru.ricardocraft.backend.socket.response.SimpleResponse;

public class GetPublicKeyResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getPublicKey";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        sendResult(new GetPublicKeyRequestEvent(keyAgreementManager.rsaPublicKey, keyAgreementManager.ecdsaPublicKey));
    }
}
