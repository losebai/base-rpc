package com.base.io.reactor;

/**
 * 处理程序
 *
 * @author bai
 * @date 2023/06/13
 */
public interface Handler<T> {


    /**
     * 过程
     */
    void process(T t);
}
