package com.base.io.reactor;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static com.base.io.reactor.Config.BUFFER_SIZE;

@Slf4j
public class ReactorServerSocketSim extends ReactorSelectIO {


    public ReactorServerSocketSim(String host, int port) {
        super(host, port);
    }


    @Override
    public void dispatch() throws IOException {
        int readyChannels = selector.select();
        if (readyChannels == 0) {
            return;
        }
        log.info(selector.selectedKeys().toString());
        Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isAcceptable()) {
                // a connection was accepted by a ServerSocketChannel.
                SocketChannel client = mainServer.accept();
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
                if (StrUtil.isEmpty(request)) {
                    key.cancel();
                    client.close();
                    log.info(client.socket().getLocalAddress().getHostAddress() + " close... " + request);
                } else {
                    buffer.flip();
                    client.write(buffer);
                    buffer.clear();
                }
            } else if (key.isWritable()) {

            }
            keyIterator.remove();
        }
    }
}
