
package com.base.rpc;

import com.base.rpc.api.DemoApi;
import com.base.rpc.api.DemoApiImpl;
import com.base.rpc.Protocol.RpcProtocol;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;


public class Provider {
    public static void main(String[] args) throws IOException {
        ProviderProcessor providerProcessor = new ProviderProcessor();
        AioQuickServer server = new AioQuickServer(8888, new RpcProtocol(), providerProcessor);
        server.start();

        providerProcessor.publishService(DemoApi.class, new DemoApiImpl());
    }
}