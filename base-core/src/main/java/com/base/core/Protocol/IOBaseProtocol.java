package com.base.core.Protocol;


import com.base.io.reactor.TCPSession;

import java.nio.ByteBuffer;

/**
 * iobase协议
 *
 * @author bai
 * @date 2023/07/18
 */
public interface IOBaseProtocol<T>  {


    /**
     * 解码
     *
     * @param readBuffer 读到缓冲区
     * @return {@link T}
     */
    T decode(TCPSession tcpSession, final ByteBuffer readBuffer);
}
