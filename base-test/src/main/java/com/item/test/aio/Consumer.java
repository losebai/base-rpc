package com.item.test.aio;

import com.base.rpc.protocol.RPCBaseProtocol;
import com.base.rpc.api.DemoApi;
import com.base.rpc.processor.RPCConsumerProcessor;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.transport.AioQuickClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class Consumer {

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



    public static void main(String[] args) throws Exception {
//        test2();
        log.debug("debug");
        log.info("info");
    }

}
