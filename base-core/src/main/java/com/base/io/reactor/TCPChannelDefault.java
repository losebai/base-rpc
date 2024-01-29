package com.base.io.reactor;

import com.base.io.common.Channel;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * tcpchannel违约
 *
 * @author bai
 * @date 2024/01/27
 */
public class TCPChannelDefault implements Channel {

    private final SocketChannel channel;


    public TCPChannelDefault(SocketChannel channel) {
        this.channel = channel;
    }


    @Override
    public void close() throws IOException {
        this.channel.close();
    }


    @Override
    public int read(ByteBuffer buffer) throws IOException {
        return this.channel.read(buffer);
    }


    @Override
    public int send(ByteBuffer buffer) throws IOException {
        return this.channel.write(buffer);
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return channel.getRemoteAddress();
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return channel.getLocalAddress();
    }


}
