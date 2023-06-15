package com.base.io.reactor;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 单线程
 * 套接字服务器sim
 *
 * @author bai
 * @date 2023/06/15
 */
@Slf4j
public class SocketServerSim {

    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws IOException {
        ReactorServerSocketSim serverSocketSim = new ReactorServerSocketSim("localhost", 7777);
        serverSocketSim.dispatch();
    }
}
