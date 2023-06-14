package com.base.io.reactor;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
public class SocketServerSim {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();

        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress("localhost", 7777);
        serverSocket.bind(address);
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int readyChannels = selector.select();
            if (readyChannels == 0) {
                continue;
            }
            log.info(selector.selectedKeys().toString());
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    // a connection was accepted by a ServerSocketChannel.
                    SocketChannel client = serverSocket.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    log.info(client.socket().getLocalAddress().getHostAddress() + " accept... ");
                } else if (key.isReadable()) {
                    // a channel is ready for reading
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    client.read(buffer);
                    String request = new String(buffer.array()).trim();
                    log.info(client.socket().getLocalAddress().getHostAddress() + " read... " + request);
                    if (StrUtil.isEmpty(request)){
                        key.cancel();
                        client.close();
                    } else {
                        buffer.flip();
                        client.write(buffer);
                        buffer.clear();
                    }

                }
                keyIterator.remove();
            }
        }
    }
}
