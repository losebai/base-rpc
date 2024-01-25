package com.item.test.nio;

import com.base.io.common.SocketServer;
import com.base.io.reactor.ReactorServerSocketSim;

import java.io.IOException;

public class ReactorServerSocketSimTest {

    public static void main(String[] args) throws IOException {
        SocketServer socketServer = new ReactorServerSocketSim("localhost", 7777);
        socketServer.start();
    }
}
