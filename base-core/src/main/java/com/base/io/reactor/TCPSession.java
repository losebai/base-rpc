package com.base.io.reactor;

import cn.hutool.core.io.IoUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

public class TCPSession {

    protected static final byte SESSION_STATUS_ACCEPT = 0;

    /**
     * Session状态:已关闭
     */
    protected static final byte SESSION_STATUS_CLOSED = 1;
    /**
     * Session状态:关闭中
     */
    protected static final byte SESSION_STATUS_CLOSING = 2;
    /**
     * Session状态:正常
     */
    protected static final byte SESSION_STATUS_ENABLED = 3;


    /**
     * 状态
     */
    protected byte status = SESSION_STATUS_ACCEPT;



    /**
     * 底层通信channel对象
     */
    private final SocketChannel channel;
    /**
     * 输出流
     */
    private final ByteBuffer writeBuffer;

    private ByteBuffer readBuffer;
    /**
     * 输出信号量,防止并发write导致异常
     */
    private final Semaphore semaphore = new Semaphore(1);


    public TCPSession(SocketChannel channel, ByteBuffer writeBuffer){
        this.channel = channel;
        this.writeBuffer = writeBuffer;
    }


    /**
     * @return 输入流
     */
    public ByteBuffer writeBuffer() {
        return writeBuffer;
    }

    public ByteBuffer readBuffer() {
        return readBuffer;
    }

    public void close() throws IOException {
        IoUtil.close(channel);
    }

    public void setStatus(byte status) {
        this.status = status;
    }
}
