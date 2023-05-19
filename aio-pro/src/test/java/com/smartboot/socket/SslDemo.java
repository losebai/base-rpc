/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: SslDemo.java
 * Date: 2020-04-16
 * Author: sandao (zhengjunweimail@163.com)
 *
 ******************************************************************************/

package com.smartboot.socket;

import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.ClientAuth;
import org.smartboot.socket.extension.ssl.factory.ClientSSLContextFactory;
import org.smartboot.socket.extension.ssl.factory.ServerSSLContextFactory;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioQuickServer;
import org.smartboot.socket.transport.AioSession;

/**
 * @author 三刀
 * @version V1.0 , 2020/4/16
 */
public class SslDemo {
    public static void main(String[] args) throws Exception {
        IntegerServerProcessor serverProcessor = new IntegerServerProcessor();
        AioQuickServer sslQuickServer = new AioQuickServer(8080, new IntegerProtocol(), serverProcessor);
        SslPlugin sslServerPlugin = new SslPlugin(new ServerSSLContextFactory(SslDemo.class.getClassLoader().getResourceAsStream("server.keystore"),"123456", "123456") , ClientAuth.OPTIONAL);
        serverProcessor.addPlugin(sslServerPlugin);
        sslQuickServer.start(configuration.group());

        IntegerClientProcessor clientProcessor = new IntegerClientProcessor();
        AioQuickClient sslQuickClient = new AioQuickClient("localhost", 8080, new IntegerProtocol(), clientProcessor);
        SslPlugin sslPlugin = new SslPlugin(new ClientSSLContextFactory(SslDemo.class.getClassLoader().getResourceAsStream("server.keystore"), "123456"));
        clientProcessor.addPlugin(sslPlugin);
//        clientProcessor.addPlugin(new SslPlugin());
        AioSession aioSession = sslQuickClient.start();
        aioSession.writeBuffer().writeInt(1);
        aioSession.writeBuffer().flush();

    }
}
