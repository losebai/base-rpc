package com.base.io.common;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * 会话
 *
 * @author bai
 * @date 2024/01/19
 */
public interface Session {

    /**
     * 得到渠道
     *
     * @return {@link Channel}
     */
    Channel getChannel();

    /**
     * 写缓冲区
     *
     * @return {@link ByteBuffer}
     */
    ByteBuffer writeBuffer();

    /**
     * 读到缓冲区
     *
     * @return {@link ByteBuffer}
     */
    ByteBuffer readBuffer();

    /**
     * 关闭
     *
     * @throws IOException ioexception
     */
    void close() throws IOException;

    /**
     * 获得地位
     */
    void getStatus();

    /**
     * 设置状态
     *
     * @param status 状态
     */
    void setStatus(byte status);

    /**
     * 获取附件对象
     *
     * @return 附件
     */
    <A> A getAttachment() ;

    /**
     * 存放附件，支持任意类型
     *
     * @param attachment 附件对象
     */
    <A> void setAttachment(A attachment);


    /**
     * @return {@link InetSocketAddress}
     * @throws IOException ioexception
     */
    SocketAddress getLocalAddress() throws IOException;

    /**
     * 获取当前会话的远程连接地址
     *
     * @return 远程地址
     * @throws IOException IO异常
     */
    SocketAddress getRemoteAddress() throws IOException;


    /**
     * 冲洗
     *
     * @throws IOException ioexception
     */
    void flush() throws IOException;
}
