
package com.base.core.decoder;

import java.nio.ByteBuffer;


public interface CommonDecoder {
    /**
     * 解码算法
     *
     * @param byteBuffer 字节缓冲区
     * @return boolean
     */
    boolean decode(ByteBuffer byteBuffer);

    /**
     * 得到缓冲
     * 获取本次解析到的完整数据
     *
     * @return {@link ByteBuffer}
     */
    ByteBuffer getBuffer();
}
