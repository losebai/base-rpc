package com.base.io.reactor;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.core.util.ThreadPoolUtil;
import com.base.io.common.Config;
import com.base.io.common.TCPProcessor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
public class ReactorSocketServer extends ReactorSelectIO {

    String hostname;

    int port;

    IOBaseProtocol<?> protocol;

    TCPProcessor<?> processor;


    private SubReactor[] subReactors;

    public <T> ReactorSocketServer(String hostname, int port, IOBaseProtocol<T> ioBaseProtocol, TCPProcessor<T> processor) {
        super(hostname, port);
        this.hostname = hostname;
        this.port = port;
        this.protocol = ioBaseProtocol;
        this.processor = processor;
    }

    public void start() throws IOException {
        synchronized (ReactorSocketServer.class) {
            if (subReactors == null) {
                subReactors = new SubReactor[Config.SubReactors_SIZE];
                // 创建subReactor 线程
                for (int i = 0; i < Config.SubReactors_SIZE; i++) {
                    subReactors[i] = new SubReactor(protocol, processor);
                    ThreadPoolUtil.submit(subReactors[i]); // 开启
                }
            }
        }
        super.start();
    }

    @Override
    public void dispatch() throws IOException {
        int readyChannels = mainSelector.select();
        log.info("mainSelector:: size :: {}", readyChannels);
        Iterator<SelectionKey> keyIterator = mainSelector.selectedKeys().iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            // 等待连接
            if (key.isAcceptable()) {
                SocketChannel client = serverChannel.accept();
                client.configureBlocking(false); // 非阻塞
                log.info("{} accept... ", client.socket().getLocalAddress().getHostAddress());
                subReactors[client.hashCode() % Config.SubReactors_SIZE].registerNewClient(client); // 注册到subReactors
            } else if (key.isConnectable()) {
                //与远程服务器建立连接。
            } else if (key.isReadable()) {
                // 可读

            } else if (key.isWritable()) {
                // 可写

            } else { // 分配给 SubReactor 处理的事件
                Event event = (Event) key.attachment();
                subReactors[event.getSubReactorId() - 1].addEvent(event);
                log.debug("{} event... ", key);
            }
        }
    }
}
