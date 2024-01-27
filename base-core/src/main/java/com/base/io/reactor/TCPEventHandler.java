package com.base.io.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * tcpevent处理程序
 *
 * @author bai
 * @date 2024/01/27
 */
public class TCPEventHandler<T> extends BaseEventHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(TCPEventHandler.class);

    private final TCPSession tcpSession;

    public TCPEventHandler(TCPSession tcpSession){
        this.tcpSession = tcpSession;
    }

    @Override
    public void onConnect() {
        log.info("TCPEventHandler onConnect");
    }

    @Override
    public void process(T b) {
        // todo TCP事件处理
        log.info("TCPEventHandler process");
    }

    @Override
    public void onOpen(T aByte) {
        log.info("TCPEventHandler onOpen");
    }

    @Override
    public void onMessage(T aByte) {
        log.info("TCPEventHandler onMessage");
    }

    @Override
    public void onClose() {
        log.info("TCPEventHandler onClose");
    }


    public TCPSession getTcpSession() {
        return tcpSession;
    }
}
