package com.base.io.common;

import java.nio.ByteBuffer;

/**
 * 事件处理程序
 * 处理程序
 *
 * @author bai
 * @date 2023/06/13
 */
public interface EventHandler<T> {

    /**
     * 在连接
     */
    void onConnect();

    /**
     * 过程
     *
     * @param t t
     */
    T process(T t);


    /**
     * 在消息
     *
     * @param t t
     */
    void onMessage(T t);


    /**
     * 可读
     *
     * @param buffer 缓冲
     */
    void readable(ByteBuffer buffer);


    /**
     * 可写
     */
    void writeable();


    /**
     * 在关闭
     */
    void onClose();
}
