package com.base.io.reactor;

import com.base.io.common.BaseConstants;
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

    public ReactorClient(String hosts, int port) throws IOException {

        selector = Selector.open();
        //连接服务器
        socketChannel = SocketChannel.open(new InetSocketAddress(hosts, port));
        //设置非阻塞
        socketChannel.configureBlocking(false);
        //将channel 注册到selector
        socketChannel.register(selector, SelectionKey.OP_READ);
        //得到username
        this.hosts = socketChannel.getLocalAddress().toString().substring(1);
        log.info(hosts + " init... ");
    }

    //向服务器发送消息
    public void send(byte[] info) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(info.length + 4);
        buffer.putInt(info.length);
        buffer.put(info);
        socketChannel.write(buffer);
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
                    log.debug(hosts + " read... " + msg);
                } else if (key.isWritable()) {
                    log.debug(hosts + " isWritable... ");
                    SocketChannel sc = (SocketChannel) key.channel();
                    sc.write(ByteBuffer.wrap(new byte[0]));
                    sc.close();
                    key.cancel();
                }
            }
            iterator.remove(); //删除当前的selectionKey, 防止重复操作
        }
    }

    @Override
    public void stop() throws IOException {
        selector.close();
    }

    public static void main(String[] args) throws Exception {
        //启动我们客户端
        ReactorClient chatClient = new ReactorClient("127.0.0.1", 7777);
        chatClient.start();
        chatClient.send("你好".getBytes());
        Thread.sleep(10);
    }
}
