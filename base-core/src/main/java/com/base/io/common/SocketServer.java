package com.base.io.common;

import java.io.IOException;

/**
 * 套接字服务器
 * 套接字服务
 *
 * @author bai
 * @date 2023/06/15
 */
public interface SocketServer {


    /**
     * 开始
     *
     * @throws IOException ioexception
     */
    void start() throws IOException;

    /**
     * 调度
     *
     * @throws IOException ioexception
     */
    void dispatch() throws IOException;


    /**
     * 停止
     *
     * @throws IOException ioexception
     */
    void stop() throws IOException;


    /**
     * 添加事件处理程序
     *
     * @param eventHandler 事件处理程序
     */
    void addEventHandler(EventHandler<?> eventHandler);
}
