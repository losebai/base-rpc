package com.base.io.common;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * 通道
 *
 * @author bai
 * @date 2024/01/27
 */
public interface Channel extends Closeable {

    /**
     * 读
     *
     * @param buffer 缓冲
     * @return int
     * @throws IOException ioexception
     */
    int read(ByteBuffer buffer) throws IOException;

    /**
     * 发送
     *
     * @param var var
     * @return int
     * @throws IOException ioexception
     */
    int send(ByteBuffer var) throws IOException;

    /**
     * @return {@link SocketAddress}
     * @throws IOException ioexception
     */
    SocketAddress getRemoteAddress() throws IOException;

    /**
     * @return {@link SocketAddress}
     * @throws IOException ioexception
     */
    SocketAddress getLocalAddress() throws IOException;

}
