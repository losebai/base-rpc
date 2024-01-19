package com.base.io.reactor;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.core.util.ThreadPoolUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * 主
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

    IOBaseProtocol<?> protocol;

    TCPProcessor<?> processor;

    //    private final static  int numSubReactors = Runtime.getRuntime().availableProcessors() >> 1; // 从的数量
    private final static int numSubReactors = 1;

    public <T> ReactorSocketServer(String hostname, int port, IOBaseProtocol<T> ioBaseProtocol, TCPProcessor<T> processor) {
        this.hostname = hostname;
        this.port = port;
        this.protocol = ioBaseProtocol;
        this.processor = processor;
    }

    public void start() throws IOException {
        mainSelector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(hostname, port);
        serverChannel.bind(address);
        serverChannel.configureBlocking(false);
        serverChannel.register(mainSelector, SelectionKey.OP_ACCEPT); // 设置可连接

        SubReactor[] subReactors = new SubReactor[numSubReactors];
        // 创建subReactor 线程
        for (int i = 0; i < numSubReactors; i++) {
            subReactors[i] = new SubReactor(protocol, processor);
            ThreadPoolUtil.submit(subReactors[i]); // 开启
        }

        while (true) {
            log.info("subReactors:: size :: {}", subReactors.length);
            int readyChannels = mainSelector.select();
            if (readyChannels == 0) {
                continue;
            }
            Iterator<SelectionKey> keyIterator = mainSelector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                // 等待连接
                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false); // 非阻塞
                    log.info("{} accept... ", client.socket().getLocalAddress().getHostAddress());
                    subReactors[client.hashCode() % numSubReactors].registerNewClient(client); // 注册到subReactors
                } else if (key.isConnectable()){
                    //与远程服务器建立连接。
                } else if (key.isReadable()) {
                    // 可读

                } else if (key.isWritable()) {
                    // 可写

                } else { // 分配给 SubReactor 处理的事件
                    Event event = (Event) key.attachment();
                    subReactors[event.getSubReactorId() - 1].addEvent(event);
                    log.info("{} event... ", key);
                }
                // 取消关联到main
            }
        }
    }

}
