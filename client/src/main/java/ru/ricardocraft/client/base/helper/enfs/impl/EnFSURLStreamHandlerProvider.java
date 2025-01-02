package ru.ricardocraft.client.base.helper.enfs.impl;

import ru.ricardocraft.client.base.helper.enfs.enfs.Handler;

import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;

public class EnFSURLStreamHandlerProvider extends URLStreamHandlerProvider {

    @Override
    public URLStreamHandler createURLStreamHandler(String s) {
        if (s.equals("enfs")) {
            return new Handler();
        }
        return null;
    }
}
