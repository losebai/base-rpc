package com.base.io.reactor;

import com.base.io.common.BaseConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

@Slf4j
@Getter
public abstract class ReactorSelectIO implements SocketServer {

    final String host;
    final int port;
    Selector selector;
    ServerSocketChannel mainServer;

    private volatile byte status = BaseConstants.status.INIT;

    ReactorSelectIO(String host, int port) {
        this.host = host;
        this.port = port;
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("建立失败");
        }
    }

    public void init() throws IOException {
        selector = Selector.open();
        mainServer = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(host, port);
        mainServer.bind(address);
        mainServer.configureBlocking(false);
        mainServer.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void start() throws IOException {
        this.status = BaseConstants.status.RUNNING;
        while (this.status == BaseConstants.status.RUNNING) {
            this.dispatch();
        }
    }

    @Override
    public void stop() {
        this.status = BaseConstants.status.STOP;
    }
}
