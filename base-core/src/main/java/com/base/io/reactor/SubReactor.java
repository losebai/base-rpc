package com.base.io.reactor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.base.io.common.ProtocolConst.CLOSE;
import static com.base.io.common.ProtocolConst.START;
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


    private final Selector selector;
    private final Queue<Event> eventQueue = new ConcurrentLinkedQueue<>();

    // 处理列表
    private final Map<SelectableChannel, EventHandler> handlerMap = new ConcurrentHashMap<>();

    public SubReactor() throws IOException {
        selector = SelectorProvider.provider().openSelector();
    }

    public void registerNewClient(SocketChannel client) throws IOException {
        EventHandler handler = new EventHandler(client);
//        LinkedList<Handler<?>> handlerLink = new LinkedList<>();
//        handlerLink.add(handler);
        handlerMap.put(client, handler); // 后期可以处理多个handler
        client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, new Event(handler)); // 将socket 注册到 从上 设置成可读
        selector.wakeup();  // 唤醒 Selector
        log.info(selector.selectedKeys().toString());
    }

    public void addEvent(Event event) {
        eventQueue.add(event);
//        selector.wakeup(); // 唤醒 Selector，以便及时处理新加入的事件
    }

    @Override
    public void run() {
        while (true) {
            try {
                log.info(Thread.currentThread() + " SubReactor start ...");
                int count = selector.select(); //
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        try {
                            // 分配给 SubReactor 处理的事件
                            log.info(key.channel()  + " dispatch... ");
                            dispatch(key);
                        }catch (IOException e){
                            key.cancel();
                            if (key.channel() != null)
                                key.channel().close();
                        }
                    }
                }
                else {
                    log.info( " event... ");
                    Event event = eventQueue.poll(); // 检索并删除此队列的头部，如果此队列为空，则返回null。返回:此队列的头部，如果此队列为空则返回null
                    if (event != null) {
                        event.getHandlers().get(0).onEvent(event.getType());
                        log.info( " onEvent... ");
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 调度
     *
     * @param key 关键
     * @throws IOException ioexception
     */
    private void dispatch(SelectionKey key) throws IOException {
        EventHandler handler = handlerMap.get(key.channel());
        if (key.isReadable()) { // read
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            int readNUm =  handler.read(buffer); // read
            log.info(handler.getHost()+ " read... " + new String(buffer.array()));
            if (readNUm > 0){
                // todo 这里可以加入编码器
                handler.process(buffer.array()); // 处理
                buffer.clear();
                if (Arrays.equals(buffer.array(), CLOSE)){
                    key.channel().close(); // 关闭后, 如果未接收数据
                    key.cancel();
                    log.info(handler.getHost()+ " read ...close " + new String(buffer.array()));
                }

            }else {
                key.channel().close(); // 关闭后, 如果未接收数据
                key.cancel();
                log.info(handler.getHost()+ " read is null ...close ");
            }
            //   写操作的就绪条件为底层缓冲区有空闲空间，而写缓冲区绝大部分时间都是有空闲空间的，所以当你注册写事件后，写操作一直是就绪的，选择处理线程全占用整个CPU资源。
            //   所以，只有当你确实有数据要写时再注册写操作，并在写完以后马上取消注册
        } else if (key.isWritable()) { // write 事件 当接收到连接时，第二次一直会触发写入事件，直到对方写入数据为止,, 慎用，可直接忽略，采用write写操作就行
            SocketChannel sc =  (SocketChannel)key.channel(); // 第一次连接后，
            ByteBuffer buffer = ByteBuffer.wrap(START);
            log.info(handler.getHost()+ " write... " + new String(START));
            if (buffer != null){
                handler.write(buffer.array());
                if (buffer.remaining() == 0) {
                    key.interestOps(SelectionKey.OP_READ);
                    handler.close(); // 关闭
                    log.info(handler.getHost()+ " close... ");
                    key.cancel(); // 取消注册
                }
            }
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }
}
