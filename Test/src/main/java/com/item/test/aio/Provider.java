
package com.item.test.aio;

import com.base.core.util.InstanceBufferPool;
import com.base.rpc.Protocol.RPCBaseProtocol;
import com.base.rpc.ProviderProcessor;
import com.base.rpc.api.DemoApi;
import com.base.rpc.api.DemoApiImpl;
import com.base.rpc.Protocol.RpcProtocol;
import com.base.rpc.processor.RPCProviderProcessor;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;


public class Provider {

    public static void test1() throws IOException {
        ProviderProcessor providerProcessor = new ProviderProcessor();
        AioQuickServer server = new AioQuickServer(8888, new RpcProtocol(), providerProcessor);
        server.start();
        providerProcessor.publishService(DemoApi.class, new DemoApiImpl());

    }

    public static void test2() throws IOException {
        RPCProviderProcessor providerProcessor = new RPCProviderProcessor();

        AioQuickServer server = new AioQuickServer(8888, new RPCBaseProtocol(), providerProcessor);
        providerProcessor.publishService(DemoApi.class, new DemoApiImpl());
        server.start();

    }

    public static void main(String[] args) throws IOException {
//        test1();
        test2();
//        AioQuickServer server = new AioQuickServer(8888, new IntegerProtocol(), serverProcessor);
    }
}