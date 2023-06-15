package com.base.io.reactor;
import com.base.core.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

/**
 *  主
 * 反应堆socket服务器
 *
 * @author bai
 * @date 2023/06/13
 */
@Slf4j
public class ReactorSocketServer {

    volatile ServerSocketChannel serverChannel;
    volatile Selector mainSelector;

    String hostname;

    int port;

//    private final static  int numSubReactors = Runtime.getRuntime().availableProcessors() >> 1;
    private final static  int numSubReactors = 1;
    public ReactorSocketServer(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public void start() throws IOException {
        mainSelector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(hostname, port);
        serverChannel.bind(address);
        serverChannel.configureBlocking(false);
        serverChannel.register(mainSelector, SelectionKey.OP_ACCEPT);


        SubReactor[] subReactors = new SubReactor[numSubReactors];
        for (int i = 0; i < numSubReactors; i++) {
            subReactors[i] = new SubReactor();
            ThreadPoolUtil.submit(subReactors[i]); // 开启
        }

        while (true) {
            log.info(Arrays.toString(subReactors));
            int readyChannels = mainSelector.select();
            if (readyChannels == 0) {
                continue;
            }
            log.info(mainSelector.selectedKeys().toString());
            Iterator<SelectionKey> keyIterator = mainSelector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                // 等待连接
                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false); // 非阻塞
                    log.info(client.socket().getLocalAddress().getHostAddress() + " accept... ");

                    subReactors[client.hashCode() % numSubReactors].registerNewClient(client); // 注册
//                    key.cancel(); // 取消注册
                } else { // 分配给 SubReactor 处理的事件
                    Event event = (Event) key.attachment();
                    subReactors[event.getSubReactorId() - 1].addEvent(event);
                    log.info(key.channel()  + " event... ");
                }
                keyIterator.remove();
            }
        }
    }

}
