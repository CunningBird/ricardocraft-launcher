package ru.ricardocraft.bff.socket.response.management;

import io.netty.channel.ChannelHandlerContext;
import ru.ricardocraft.bff.base.events.request.GetPublicKeyRequestEvent;
import ru.ricardocraft.bff.socket.Client;
import ru.ricardocraft.bff.socket.response.SimpleResponse;

public class GetPublicKeyResponse extends SimpleResponse {
    @Override
    public String getType() {
        return "getPublicKey";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) {
        sendResult(new GetPublicKeyRequestEvent(server.keyAgreementManager.rsaPublicKey, server.keyAgreementManager.ecdsaPublicKey));
    }
}
