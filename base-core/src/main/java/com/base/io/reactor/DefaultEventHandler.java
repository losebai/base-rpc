package com.base.io.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * tcpevent处理程序
 *
 * @author bai
 * @date 2024/01/27
 */
public class DefaultEventHandler<T> extends BaseEventHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(DefaultEventHandler.class);

    private final TCPSession tcpSession;

    public DefaultEventHandler(TCPSession tcpSession){
        this.tcpSession = tcpSession;
    }

    @Override
    public void onConnect() {
        log.info("DefaultEventHandler onConnect");
    }

    @Override
    public T process(T b) {
        // todo TCP事件处理
        log.info("DefaultEventHandler process");
        return b;
    }


    @Override
    public void onMessage(T aByte) {
        log.info("DefaultEventHandler onMessage");
    }

    @Override
    public void onClose() {
        log.info("DefaultEventHandler onClose");
    }


    public TCPSession getTcpSession() {
        return tcpSession;
    }
}
