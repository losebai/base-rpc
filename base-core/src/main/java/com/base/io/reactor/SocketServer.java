package com.base.io.reactor;

import java.io.IOException;

/**
 * 套接字服务器
 *
 * @author bai
 * @date 2023/06/15
 */
public interface SocketServer {


    /**
     * 调度
     */
    void dispatch() throws IOException;
}
