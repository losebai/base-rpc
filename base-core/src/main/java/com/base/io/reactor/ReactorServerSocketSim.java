package com.base.io.reactor;

import cn.hutool.core.util.StrUtil;
import com.base.io.common.Config;
import com.base.io.common.SocketServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
public class ReactorServerSocketSim extends ReactorSelectIO {


    public ReactorServerSocketSim(String host, int port) {
        super(host, port);
    }


    @Override
    public void dispatch() throws IOException {
        int readyChannels = mainSelector.select();
        if (readyChannels == 0) {
            return;
        }
        log.info(mainSelector.selectedKeys().toString());
        Iterator<SelectionKey> keyIterator = mainSelector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            if (key.isAcceptable()) {
                // a connection was accepted by a ServerSocketChannel.
                SocketChannel client = serverChannel.accept();
                client.configureBlocking(false);
                client.register(mainSelector, SelectionKey.OP_READ);
                log.info(client.socket().getLocalAddress().getHostAddress() + " accept... ");
            } else if (key.isReadable()) {
                // a channel is ready for reading
                SocketChannel client = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(Config.READ_BUFFER_SIZE);
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
                key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            }
            keyIterator.remove();
        }
    }



}
