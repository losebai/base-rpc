package com.base.io.reactor;


import cn.hutool.core.util.StrUtil;
import com.base.io.common.uitl.ThreadPoolUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.base.io.reactor.Config.BUFFER_SIZE;

/**
 * 子核反应堆
 *
 * @author bai
 * @date 2023/06/13
 */
@Getter
@Slf4j
public class SubReactor implements  Runnable{


    private volatile Selector selector;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();
    private final Map<SelectableChannel, EventHandler> handlerMap = new ConcurrentHashMap<>();

    public SubReactor() throws IOException {
        selector = Selector.open();
        ThreadPoolUtil.submit(this);
    }

    public void registerNewClient(Selector mainSelector ,SocketChannel client) throws ClosedChannelException {
        EventHandler handler = new EventHandler(client);
        handlerMap.put(client, handler); // 后期可以处理多个handler
        client.register(selector, SelectionKey.OP_READ, new Event(handler)); // 将socket 注册到 从上 设置成可读
        log.info(selector.selectedKeys().toString());
    }

    public void addEvent(Event event) {
        eventQueue.add(event);
        selector.wakeup(); // 唤醒 Selector，以便及时处理新加入的事件
    }

    @Override
    public void run() {
        while (true) {
            try {
                log.info(Thread.currentThread() + " SubReactor start ...");
                int count = selector.select();
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        EventHandler handler = handlerMap.get(key.channel());
                        if (key.isReadable()) { // read
                            log.info(handler.getHost()+ " read... ");
                            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                            int readNUm =  handler.read(buffer);
                            if (readNUm > 0){
                                log.debug(new String(handler.getReadBuffer().array()));
                                handler.process(buffer.array()); // 处理
                                key.interestOps(SelectionKey.OP_WRITE);
                            }else {
                                key.cancel(); // 取消注册
                                handler.close(); // 关闭
                                log.info(handler.getHost()+ " close... ");
                            }

                        } else if (key.isWritable()) { // write
                            ByteBuffer buffer = handler.getWriteBuffer();
                            handler.write();
                            log.info(handler.getHost()+ " write... ");
                            if (buffer.remaining() == 0) {
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        }
                        iterator.remove();
                    }
                } else {
                    Event event = eventQueue.poll();
                    if (event != null) {
                        event.getHandler().onEvent(event.getType());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
