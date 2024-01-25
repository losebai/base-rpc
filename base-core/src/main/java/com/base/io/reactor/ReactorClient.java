package com.base.io.reactor;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.io.common.BaseConstants;
import com.base.io.common.Config;
import com.base.io.common.SocketServer;
import com.base.io.common.TCPProcessor;
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

    private volatile byte status = BaseConstants.status.INIT;

    public<T> ReactorClient(String hosts, int port, IOBaseProtocol<T> ioBaseProtocol, TCPProcessor<T> processor) throws IOException {

        selector = Selector.open();
        //连接服务器
        socketChannel = SocketChannel.open(new InetSocketAddress(hosts, port));
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //将channel 注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        //得到username
        this.hosts = socketChannel.getLocalAddress().toString().substring(1);
        log.info(hosts + " init... ");
    }

    //向服务器发送消息
    public void send(byte[] info) throws IOException {
        if (!socketChannel.isOpen()){
            log.info("{}链接已关闭", socketChannel);
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
        Runnable runnable = () ->{
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
        if (readChannels > 0) {//有可以用的通道
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isReadable()) {
                    log.info(hosts + " isReadable... ");
                    //得到相关的通道
                    SocketChannel sc = (SocketChannel) key.channel();
                    //得到一个Buffer
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    //读取
                    sc.read(buffer);
                    if (!buffer.hasRemaining()) {
                        key.cancel();
                        sc.close();
                    }
                    //把读到的缓冲区的数据转成字符串
                    String msg = new String(buffer.array());
                    buffer.clear();
                    log.info(hosts + " read... " + msg);
                } else if (key.isWritable()) {
                    log.info(hosts + " isWritable... ");
                    // 取消写入事件
                    key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
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
    }


}
