package com.base.http;

import java.io.IOException;
import java.nio.ByteBuffer;


public interface Handler<T> {

    /**
     * 解析 body 数据流
     *
     * @param buffer  缓冲
     * @param request 请求
     * @return boolean
     */
    boolean onBodyStream(ByteBuffer buffer, T request);


    /**
     * Http header 完成解析
     */
    default void onHeaderComplete(T request) throws IOException {
    }

    /**
     * 断开 TCP 连接
     */
    default void onClose(T request) {
    }
}
