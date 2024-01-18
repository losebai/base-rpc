package com.base.io.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public interface Channel {

    void open();

    void close();

    byte[] read(int size);

    void read(ByteBuffer buffer);

    void send(byte[] bytes);

    void send(String str);

    InetSocketAddress getRemoteAddress() throws IOException;

    InetSocketAddress getLocalAddress() throws IOException;

}
