package com.base.io.reactor;

import com.base.io.common.BaseConstants;
import com.base.io.common.Config;
import com.base.io.common.EventHandler;
import com.base.io.common.SocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class ReactorClient implements SocketServer {

    private static final Logger log = LoggerFactory.getLogger(ReactorClient.class);


    private final Selector selector;
    private final SocketChannel socketChannel;
    private final String hosts;
    private final SelectionKey selectionKey;

    private volatile byte status = BaseConstants.status.INIT;

    private final TCPSelectorIO tcpSelectorIO;

    private final TCPSession tcpSession;

    public <T> ReactorClient(String hosts, int port, TCPSelectorIO<T> tcpSelectorIO) throws IOException {
        this.tcpSelectorIO = tcpSelectorIO;
        selector = Selector.open();
        //连接服务器
        socketChannel = SocketChannel.open(new InetSocketAddress(hosts, port));
        tcpSession = new TCPSession(socketChannel, ByteBuffer.allocate(Config.READ_BUFFER_SIZE), ByteBuffer.allocate(Config.WRITE_BUFFER_SIZE));
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //将channel 注册到selector
        selectionKey = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, tcpSession);
        //得到username
        this.hosts = socketChannel.getLocalAddress().toString().substring(1);
        BaseEventHandler<T> baseEventHandler = new DefaultEventHandler<>(tcpSession);
        tcpSelectorIO.setHandlerMap(selectionKey.channel(), baseEventHandler); // 后期可以处理多个handler
        baseEventHandler.onConnect();
        log.info(hosts + " init... ");
    }

    //向服务器发送消息
    public void send(byte[] info) throws IOException {
        if (!socketChannel.isOpen()) {
            log.info("{} 链接已关闭", socketChannel);
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(Config.WRITE_BUFFER_SIZE);
        buffer.putInt(info.length);
        buffer.put(info);
        buffer.flip();
        socketChannel.write(buffer);
        log.info("write size {}, msg {}", info.length, buffer);
    }


    //读取从服务器端回复的消息
    public void start() throws IOException {
        this.status = BaseConstants.status.RUNNING;
        Runnable runnable = () -> {
            while (status == BaseConstants.status.RUNNING) {
                try {
                    this.dispatch();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        Thread readThread = new Thread(runnable);
        readThread.start();
    }

    @Override
    public void dispatch() throws IOException {
        int readChannels = selector.select();
        //有可以用的通道
        if (readChannels > 0) {
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                TCPSession tcpSession = (TCPSession) key.attachment(); // 对应注册时，传入的对象 第一次连接后
                try {
                    // 分配给 SubReactor 处理的事件
                    log.info(key.channel() + " handler... ");
                    // io处理回调
                    tcpSelectorIO.handler(key, tcpSession);
                } catch (IOException e) {
                    log.error(e.getMessage());
                    key.cancel(); // 取消注册
                    if (key.channel() != null)
                        key.channel().close();
                    // io 异常处理
                    tcpSelectorIO.error(key, tcpSession);
                    throw e;
                }
            }
            iterator.remove(); //删除当前的selectionKey, 防止重复操作
        }
    }

    @Override
    public void stop() throws IOException {
        this.status = BaseConstants.status.STOP;
        socketChannel.close(); // 触发c->write事件
        selector.close();
        tcpSelectorIO.close(selectionKey, tcpSession);
    }

    @Override
    public void addEventHandler(EventHandler<?> eventHandler) {

    }

    public Selector getSelector() {
        return selector;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public String getHosts() {
        return hosts;
    }

    public byte getStatus() {
        return status;
    }

}
