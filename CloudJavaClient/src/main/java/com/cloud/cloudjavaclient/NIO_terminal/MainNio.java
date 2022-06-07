package com.cloud.cloudjavaclient.NIO_terminal;

import java.io.IOException;

public class MainNio {
    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }
}
