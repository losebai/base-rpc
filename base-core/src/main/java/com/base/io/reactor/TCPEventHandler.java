package com.base.io.reactor;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TCPEventHandler extends BaseEventHandler<Byte> {


    public TCPEventHandler(SocketChannel channel) throws IOException {
        super(channel);
    }

    @Override
    public void process(Byte b) {


    }
}
