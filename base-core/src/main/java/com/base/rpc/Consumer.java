package com.base.rpc;

import com.base.rpc.api.DemoApi;
import com.base.rpc.Protocol.RpcProtocol;
import org.smartboot.socket.transport.AioQuickClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Consumer {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        ConsumerProcessor consumerProcessor = new ConsumerProcessor();
        AioQuickClient consumer = new AioQuickClient("localhost", 8888, new RpcProtocol(), consumerProcessor);
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

}
