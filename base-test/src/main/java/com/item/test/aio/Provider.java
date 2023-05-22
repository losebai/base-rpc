
package com.item.test.aio;

import com.base.http.processor.HttpResponseProcessor;
import com.base.http.protocol.HttpResponseProtocol;
import com.base.rpc.protocol.RPCBaseProtocol;
import com.base.rpc.api.DemoApi;
import com.base.rpc.api.DemoApiImpl;
import com.base.rpc.processor.RPCProviderProcessor;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;


public class Provider {


    public static void test2() throws IOException {
        RPCProviderProcessor providerProcessor = new RPCProviderProcessor();

        AioQuickServer server = new AioQuickServer(8888, new RPCBaseProtocol(), providerProcessor);
        providerProcessor.pushImplClass(DemoApi.class, DemoApiImpl.class);
        server.start();
    }

    public static void test3() throws IOException {
        HttpResponseProcessor providerProcessor = new HttpResponseProcessor();

        AioQuickServer server = new AioQuickServer(8888, new HttpResponseProtocol(), providerProcessor);
        server.start();
    }



    public static void main(String[] args) throws IOException {
//        test1();
        test3();
//        AioQuickServer server = new AioQuickServer(8888, new IntegerProtocol(), serverProcessor);
    }
}