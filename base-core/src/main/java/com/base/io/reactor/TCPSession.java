package com.base.io.reactor;

import cn.hutool.core.io.IoUtil;
import com.base.io.common.Channel;
import com.base.io.common.Session;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Semaphore;

public class TCPSession implements Session {

    /**
     * 会话状态接受
     */
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
     * 解码例外
     */
    protected static final byte DECODE_EXCEPTION = 11;

    /**
     * 过程异常
     */
    protected static final byte PROCESS_EXCEPTION = 12;

    /**
     * 状态
     */
    protected byte status = SESSION_STATUS_ACCEPT;


    /**
     * 底层通信channel对象
     */
    private final Channel channel;

    /**
     * 写缓冲区
     */
    private ByteBuffer writeBuffer;

    /**
     * 读到缓冲区
     */
    private final ByteBuffer readBuffer;


    /**
     * 输出信号量,防止并发write导致异常
     */
    private final Semaphore semaphore = new Semaphore(1);


    public TCPSession(SocketChannel channel, ByteBuffer readBuffer){
        this.channel = new TCPChannelDefault(channel);
        this.readBuffer = readBuffer;
    }


    @Override
    public Channel getChannel() {
        return channel;
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

    @Override
    public void getStatus() {

    }

    public void setWriteBuffer(ByteBuffer writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public <A> A getAttachment() {
        return null;
    }

    @Override
    public <A> void setAttachment(A attachment) {

    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return channel.getLocalAddress();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return channel.getRemoteAddress();
    }

    public boolean isClose(){
        return status == SESSION_STATUS_CLOSED;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
}
