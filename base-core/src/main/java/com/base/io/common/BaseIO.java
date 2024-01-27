package com.base.io.common;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * 基本输入输出
 *
 * @author bai
 * @date 2024/01/27
 */
public interface BaseIO<T, Session> {

    /**
     * 连接
     *
     * @param t       t
     * @param session 会话
     */
    void connect(T t, Session session);

    /**
     * 处理程序
     *
     * @param t       t
     * @param session 会话
     * @throws IOException ioexception
     */
    void handler(T t, Session session) throws IOException;

    /**
     * 关闭
     *
     * @param t       t
     * @param session 会话
     * @throws IOException ioexception
     */
    void close(T t, Session session) throws IOException;

    /**
     * 错误
     *
     * @param t       t
     * @param session 会话
     */
    void error(T t, Session session);

    /**
     * @param selectionKey
     * @param event
     * @return boolean
     */
    default boolean isInterest(SelectionKey selectionKey, int event) {
        int interestSet = selectionKey.interestOps();
        return (interestSet & event) == event;
    }
}
