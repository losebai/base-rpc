package com.item.test.aio;

import com.base.rpc.processor.ConsumerProcessor;
import com.base.rpc.Protocol.RPCBaseProtocol;
import com.base.rpc.api.DemoApi;
import com.base.rpc.Protocol.ByteProtocol;
import com.base.rpc.processor.RPCConsumerProcessor;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.transport.AioQuickClient;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Consumer {


    public static void test1() throws Exception {

        ConsumerProcessor consumerProcessor = new ConsumerProcessor();
        AioQuickClient consumer = new AioQuickClient("localhost", 8888,
                new ByteProtocol(), consumerProcessor);
        consumer.start();

        DemoApi demoApi = consumerProcessor.getObject(DemoApi.class);
        ExecutorService pool= Executors.newCachedThreadPool();
        pool.execute(()->{
            System.out.println(demoApi.test("smart-socket"));
        });
        pool.execute(()->{
            System.out.println(demoApi.test("smart-socket2"));
        });
        pool.execute(()->{
            System.out.println(demoApi.sum(1, 2));
        });
    }
    public static void test2() throws Exception {
        RPCConsumerProcessor consumerProcessor = new RPCConsumerProcessor();
        AioQuickClient consumer = new AioQuickClient("localhost", 8888,
                new RPCBaseProtocol(), consumerProcessor);
        consumer.start();

        DemoApi demoApi = consumerProcessor.getObject(DemoApi.class);
        ExecutorService pool= Executors.newCachedThreadPool();
        pool.execute(()->{
            System.out.println(demoApi.test("smart-socket1"));
            System.out.println(demoApi.test("smart-socket2"));
            System.out.println(demoApi.sum(1, 2));
        });

    }

    public static void test3() throws Exception {

        ConsumerProcessor consumerProcessor = new ConsumerProcessor();
        AioQuickClient consumer = new AioQuickClient("localhost", 8888,
                new ByteProtocol(), consumerProcessor);
        AioSession aioSession = consumer.start();
        WriteBuffer writeBuffer = aioSession.writeBuffer();
        String http =  "GET /tool-admin/intelligentuser/page?page=1&limit=1000&_t=1684464554812 HTTP/1.1\n" +
                "Accept: application/json, text/plain, */*\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Accept-Language: zh-CN\n" +
                "Connection: keep-alive\n" +
                "Cookie: JSESSIONID=0cc1b89f-82d1-4612-85f3-a37a01f1b26f; language=zh-CN; token=2eddb49ae21033126441060f141fb7f0\n" +
                "Host: 139.224.237.182:32215\n" +
                "Origin: http://139.224.237.182:32293\n" +
                "Referer: http://139.224.237.182:32293/\n" +
                "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.46\n" +
                "token: 2eddb49ae21033126441060f141fb7f0";
        writeBuffer.write(http.getBytes());
        writeBuffer.writeInt(http.getBytes().length);
        writeBuffer.flush();
    }


    public static void main(String[] args) throws Exception {
//        test2();
        test3();
        log.debug("debug");
        log.info("info");
    }

}
