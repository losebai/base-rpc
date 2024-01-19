package com.base.io.common;

/**
 * 处理程序
 *
 * @author bai
 * @date 2023/06/13
 */
public interface EventHandler<T> {


    /**
     * 过程
     */
    void process(T t);

    void onOpen(T t);

    void onMessage(T t);

    void onRead(T t);

    void onWrite(T t);


    void onClose();
}
