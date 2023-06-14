package com.base.io.common;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * iochannel
 *
 * @author bai
 * @date 2023/06/13
 */
public interface ReactorHandler {


    /**
     * 在阅读
     *
     * @param socketChannel 套接字通道
     * @param buffer        缓冲
     * @return int
     */
    int onRead(SocketChannel socketChannel, ByteBuffer buffer);


    /**
     * 写
     *
     * @param socketChannel 套接字通道
     * @param buffer        缓冲
     * @return int
     */
    int write(SocketChannel socketChannel, ByteBuffer buffer);
}
