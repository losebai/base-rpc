package com.base.io.common;

import com.base.io.reactor.TCPSession;

@FunctionalInterface
public interface TCPProcessor<T> {


    /**
     * 处理接收到的消息
     *
     * @param session 通信会话
     * @param msg     待处理的业务消息
     */
    void process(TCPSession session, T msg);
}
