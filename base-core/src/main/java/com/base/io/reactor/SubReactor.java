package com.base.io.reactor;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.io.common.BaseConstants;
import com.base.io.common.Config;
import com.base.io.common.EventHandler;
import com.base.io.common.SocketServer;
import com.base.io.common.TCPProcessor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 子核反应堆, 用于处理链接整体事件
 *
 * @author bai
 * @date 2023/06/13
 */
@Getter
@Slf4j
public class SubReactor<T> extends TCPSelectorIO<T> implements Runnable, SocketServer {


    private final Selector selector;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();


    private volatile byte status = BaseConstants.status.INIT;

    public SubReactor(IOBaseProtocol<T> protocol, TCPProcessor<T> processor) throws IOException {
        super(protocol, processor);
        selector = SelectorProvider.provider().openSelector();
    }

    public void registerNewClient(SocketChannel client) throws IOException {
        client.configureBlocking(false); // 非阻塞
        TCPSession tcpSession = new TCPSession(client, ByteBuffer.allocate(Config.READ_BUFFER_SIZE), ByteBuffer.allocate(Config.WRITE_BUFFER_SIZE));
        BaseEventHandler<T> baseEventHandler = new TCPEventHandler<T>(tcpSession);
        handlerMap.put(client, baseEventHandler); // 后期可以处理多个handler
        // 将socket 注册到 从上 设置成可读
        client.register(selector, SelectionKey.OP_READ, tcpSession);
        selector.wakeup();  // 唤醒 Selector
        log.info(selector.selectedKeys().toString());
    }

    public void addEvent(Event event) {
        eventQueue.add(event);
//        mainSelector.wakeup(); // 唤醒 Selector，以便及时处理新加入的事件
    }

    @Override
    public void start() throws IOException {
        status = BaseConstants.status.RUNNING;
        while (status == BaseConstants.status.RUNNING) {
            log.info("mainSelector {} ...handler ", selector.toString());
            this.dispatch();
        }
    }

    @Override
    public void dispatch() throws IOException {
        try {
            int count = selector.select(); //
            log.info(Thread.currentThread() + " SubReactor select count :{}...", count);
            if (count > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    TCPSession tcpSession = (TCPSession) key.attachment(); // 对应注册时，传入的对象 第一次连接后，
                    // io链接回调
                    super.connect(key, tcpSession);
                    try {
                        // 分配给 SubReactor 处理的事件
                        log.info(key.channel() + " handler... ");
                        // io处理回调
                        super.handler(key, tcpSession);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        key.cancel(); // 取消注册
                        if (key.channel() != null)
                            key.channel().close();
                        // io 异常处理
                        super.error(key, tcpSession);
                        throw e;
                    }
                    if (tcpSession.status == TCPSession.SESSION_STATUS_CLOSED){
                        key.cancel();
                        // io 链接关闭
                        super.close(key, tcpSession);
                        break;
                    }
                }
            } else {
                // 被唤醒之后，select 成非阻塞的
                log.info(" event... ");
                Event event = eventQueue.poll(); // 检索并删除此队列的头部，如果此队列为空，则返回null。返回:此队列的头部，如果此队列为空则返回null
                if (event != null) {
//                    event.getHandler().onEvent(event.getType());
                    log.info(" Event end... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (log.isDebugEnabled()){
                throw e;
            }
        }
    }

    @Override
    public void addEventHandler(EventHandler<?> eventHandler) {

    }

    @Override
    public void stop() throws IOException {
        selector.close();
        status = BaseConstants.status.STOP;
    }


    @Override
    public void run() {
        try {
            this.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
