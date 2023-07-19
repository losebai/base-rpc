package com.base.core.Protocol;


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
    T decode(final ByteBuffer readBuffer);
}
