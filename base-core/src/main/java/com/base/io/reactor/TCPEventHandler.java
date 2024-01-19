package com.base.io.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TCPEventHandler extends BaseEventHandler<Byte> {

    private static final Logger log = LoggerFactory.getLogger(TCPEventHandler.class);

    private final TCPSession tcpSession;

    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();

    public TCPEventHandler(TCPSession tcpSession) throws IOException {
        this.tcpSession = tcpSession;
    }

    @Override
    public void process(Byte b) {
        // todo TCP事件处理
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        return tcpSession.getChannel().read(buffer);
    }

    @Override
    public int write(ByteBuffer buffer) throws IOException {
        return tcpSession.getChannel().send(buffer);
    }

    @Override
    public void flush() throws IOException {
        if (tcpSession.isClose()){
            throw new RuntimeException("tcpSession is closed");
        }
        if (tcpSession.getSemaphore().tryAcquire()) {
            ByteBuffer buffer = tcpSession.writeBuffer();
            if (buffer != null){
                log.info("{} send {}", tcpSession.getChannel(), buffer);
                tcpSession.getChannel().send(buffer);
            }
            tcpSession.getSemaphore().release();
        }
    }

    public TCPSession getTcpSession() {
        return tcpSession;
    }
}
