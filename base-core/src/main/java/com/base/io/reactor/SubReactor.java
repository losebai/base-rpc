package com.base.io.reactor;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.io.common.BaseConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.base.io.common.ProtocolConst.*;
import static com.base.io.reactor.Config.BUFFER_SIZE;
import static com.base.io.reactor.TCPSession.SESSION_STATUS_ENABLED;

/**
 * 子核反应堆
 *
 * @author bai
 * @date 2023/06/13
 */
@Getter
@Slf4j
public class SubReactor implements Runnable, SocketServer {


    private final Selector selector;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    // 处理列表
    private final Map<SelectableChannel, BaseEventHandler<?>> handlerMap = new ConcurrentHashMap<>();

    private final IOBaseProtocol protocol;

    private final TCPProcessor processor;

    private volatile byte status = BaseConstants.status.INIT;

    public SubReactor(IOBaseProtocol<?> protocol, TCPProcessor<?> processor) throws IOException {
        selector = SelectorProvider.provider().openSelector();
        this.protocol = protocol;
        this.processor = processor;
    }

    public void registerNewClient(SocketChannel client) throws IOException {
        BaseEventHandler<Byte> baseEventHandler = new TCPEventHandler(client);
        handlerMap.put(client, baseEventHandler); // 后期可以处理多个handler
        TCPSession tcpSession = new TCPSession(client, ByteBuffer.wrap(READ));
        // 将socket 注册到 从上 设置成可读 可写
        client.register(selector, SelectionKey.OP_READ, tcpSession);
        selector.wakeup();  // 唤醒 Selector
        log.info(selector.selectedKeys().toString());
    }

    public void addEvent(Event event) {
        eventQueue.add(event);
//        selector.wakeup(); // 唤醒 Selector，以便及时处理新加入的事件
    }

    @Override
    public void start() throws IOException {
        status = BaseConstants.status.RUNNING;
        while (status == BaseConstants.status.RUNNING) {
            log.info("selector {} ...handler ", selector.toString());
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
                    try {
                        // 分配给 SubReactor 处理的事件
                        log.info(key.channel() + " handler... ");
                        handler(key);
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        key.cancel(); // 取消注册
                        if (key.channel() != null)
                            key.channel().close();
                    }
                }
            } else {
                // 被唤醒之后，select 成非阻塞的
                log.info(" event... ");
                Event event = eventQueue.poll(); // 检索并删除此队列的头部，如果此队列为空，则返回null。返回:此队列的头部，如果此队列为空则返回null
                if (event != null) {
                    event.getHandler().onEvent(event.getType());
                    log.info(" Event end... ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    /**
     * 处理程序
     *
     * @param key 关键
     * @throws IOException ioexception
     */
    private void handler(SelectionKey key) throws IOException {
        BaseEventHandler<?> handler = handlerMap.get(key.channel());
        TCPSession tcpSession = (TCPSession) key.attachment(); // 对应注册时，传入的对象 第一次连接后，
        if (key.isConnectable()) {
            //与远程服务器建立连接。

        } else if (key.isReadable()) { // read
            log.info(handler.getHost() + " read... ");
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            int readNum = handler.read(buffer); // 从缓冲区中读取
            while (buffer.hasRemaining() && readNum > 0) {
                Object object = null;
                try {
                    // todo 这里可以加入编码器
                    object = protocol.decode(tcpSession, buffer);

                } catch (Exception e) {
                    tcpSession.setStatus(TCPSession.SESSION_STATUS_CLOSING);
                }
                if (object == null) {
                    break;
                }
                processor.process(tcpSession, object);
                if (tcpSession.status == TCPSession.SESSION_STATUS_CLOSING) {
                    key.channel().close(); // 关闭后, 如果未接收数据
                    key.cancel();
                    log.info(handler.getHost() + " read ...close..... ");
                }
                buffer.clear();
                log.info(handler.getHost() + " read... {}", object);
            }
            // 读完数据后，为 SelectionKey 注册可写事件
            if (!isInterest(key, SelectionKey.OP_WRITE)) {
                key.interestOps(key.interestOps() + SelectionKey.OP_WRITE);
            }
            // 压缩读缓冲区
            buffer.compact();
            //   写操作的就绪条件为底层缓冲区有空闲空间，而写缓冲区绝大部分时间都是有空闲空间的，所以当你注册写事件后，写操作一直是就绪的，选择处理线程全占用整个CPU资源。
            //   所以，只有当你确实有数据要写时再注册写操作，并在写完以后马上取消注册
        } else if (key.isWritable()) { // write 事件 当接收到连接时，第二次一直会触发写入事件，直到对方写入数据为止,, 慎用，可直接忽略，采用write写操作就行
            SocketChannel sc =  (SocketChannel)key.channel();
            tcpSession.setStatus(SESSION_STATUS_ENABLED); // 连接成功
            log.info(handler.getHost() + " write... ");
            // 写完数据后，要把写事件取消，否则当写缓冲区有剩余空间时，会一直触发写事件
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            sc.write(ByteBuffer.allocate(1));
        }
    }

    private static boolean isInterest(SelectionKey selectionKey, int event) {
        int interestSet = selectionKey.interestOps();
        return (interestSet & event) == event;
    }
}
