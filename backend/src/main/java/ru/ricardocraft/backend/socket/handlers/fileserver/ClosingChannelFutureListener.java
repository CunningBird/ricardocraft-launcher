package ru.ricardocraft.backend.socket.handlers.fileserver;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import ru.ricardocraft.backend.base.helper.IOHelper;

import java.io.Closeable;

public class ClosingChannelFutureListener implements ChannelFutureListener {
    public final Closeable[] close;

    public ClosingChannelFutureListener(Closeable... close) {
        this.close = close;
    }


    @Override
    public void operationComplete(ChannelFuture future) {
        for (Closeable cl : close) IOHelper.close(cl);
    }

}
