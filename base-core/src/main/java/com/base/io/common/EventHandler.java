package com.base.io.common;

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
    void process(T t);

    /**
     * 在开放
     *
     * @param t t
     */
    void onOpen(T t);

    /**
     * 在消息
     *
     * @param t t
     */
    void onMessage(T t);


    /**
     * 在关闭
     */
    void onClose();
}
