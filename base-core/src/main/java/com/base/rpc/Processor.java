package com.base.rpc;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioSession;


public interface Processor<T> extends MessageProcessor<T> {


    /**
     * 处理接收到的消息
     *
     * @param session 通信会话
     * @param msg     待处理的业务消息
     */
    void process(AioSession session, T msg);
}
