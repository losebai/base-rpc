package com.base.io.common;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface Channel extends Closeable {

    void close() throws IOException;

    int read(ByteBuffer buffer) throws IOException;

    int send(ByteBuffer var) throws IOException;

    SocketAddress getRemoteAddress() throws IOException;

    SocketAddress getLocalAddress() throws IOException;

}
