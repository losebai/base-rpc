/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: Consumer.java
 * Date: 2021-02-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 ******************************************************************************/

package com.base.rpc;

import com.base.rpc.api.DemoApi;
import com.base.rpc.io.RpcProtocol;
import org.smartboot.socket.transport.AioQuickClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 三刀
 * @version V1.0 , 2018/7/1
 */
public class Consumer {

    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        RpcConsumerProcessor rpcConsumerProcessor = new RpcConsumerProcessor();
        AioQuickClient consumer = new AioQuickClient("localhost", 8888, new RpcProtocol(), rpcConsumerProcessor);
        consumer.start();

        DemoApi demoApi = rpcConsumerProcessor.getObject(DemoApi.class);
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
