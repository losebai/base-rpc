package com.base.io.reactor;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

@Slf4j
public class ReactorClient {


    private final Selector selector;
    private final SocketChannel socketChannel;
    private final String hosts;

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
        log.info(hosts+ " init... ");
    }

    //向服务器发送消息
    public void sendInfo(String info) {
        log.info(hosts+ " send... " + info);
        try {
            if (info.equals("\\n")){
                socketChannel.close();
            }else {
                socketChannel.write(ByteBuffer.wrap(info.getBytes()));
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //读取从服务器端回复的消息
    public void readInfo() {
        try {
            int readChannels = selector.select();
            if(readChannels > 0) {//有可以用的通道
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if(key.isReadable()) {
                        log.info(hosts+ " isReadable... ");
                        //得到相关的通道
                        SocketChannel sc = (SocketChannel) key.channel();
                        //得到一个Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //读取
                        sc.read(buffer);
                        if (!buffer.hasRemaining()){
                            key.cancel();
                            sc.close();
                        }
                        //把读到的缓冲区的数据转成字符串
                        String msg = new String(buffer.array());
                        buffer.clear();
                        log.info(hosts+ " read... " + msg);
                    }else if (key.isWritable()){
                        log.info(hosts+ " isWritable... " );
                        SocketChannel sc = (SocketChannel) key.channel();
                        sc.write(ByteBuffer.wrap(new byte[0]));
                        sc.close();
                        key.cancel();
                    }
                }
                iterator.remove(); //删除当前的selectionKey, 防止重复操作
            } else {
                //System.out.println("没有可以用的通道...");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        //启动我们客户端
        ReactorClient chatClient = new ReactorClient("127.0.0.1",7777);
        //启动一个线程, 每个3秒，读取从服务器发送数据
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    chatClient.readInfo();
//                    try {
//                        sleep(3000);
//                    }catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }.start();

        //发送数据给服务器端
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            chatClient.sendInfo(s);
        }
    }
}
