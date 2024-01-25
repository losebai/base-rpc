package com.item.test.nio;

import com.base.io.reactor.ReactorClient;

/**
 * 反应堆客户试验
 *
 * @author bai
 * @date 2024/01/19
 */
public class ReactorClientTest {

    public static void main(String[] args) throws Exception {
        //启动我们客户端
        ReactorClient chatClient = new ReactorClient("127.0.0.1", 7777);
        chatClient.start();
        chatClient.send("你好1".getBytes());
        chatClient.send("你好2".getBytes());
        chatClient.send("你好3".getBytes());
        chatClient.send("你好4".getBytes());
        Thread.sleep(1000);
        chatClient.stop();
    }
}
