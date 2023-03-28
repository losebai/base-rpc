package com.item.test.aio;

import com.base.core.util.ClassLoaderMapperUtil;
import com.base.rpc.ConsumerProcessor;
import com.base.rpc.Protocol.RPCBaseProtocol;
import com.base.rpc.api.DemoApi;
import com.base.rpc.Protocol.RpcProtocol;
import com.base.rpc.processor.RPCConsumerProcessor;
import org.smartboot.socket.transport.AioQuickClient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Consumer {


    public static void test1() throws Exception {

        RPCConsumerProcessor consumerProcessor = new RPCConsumerProcessor();
        AioQuickClient consumer = new AioQuickClient("localhost", 8888,
                new RPCBaseProtocol(), consumerProcessor);
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

    public static void main(String[] args) throws Exception {
        test1();

    }

}
