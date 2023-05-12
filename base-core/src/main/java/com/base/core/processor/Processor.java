package com.base.core.processor;

import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioSession;


/**
 * 处理器
 *
 * @author bai
 * @date 2023/03/17
 */
public interface Processor<T> extends MessageProcessor<T> {


    /**
     * 处理接收到的消息
     *
     * @param session 通信会话
     * @param msg     待处理的业务消息
     */
    void process(AioSession session, T msg);
}
