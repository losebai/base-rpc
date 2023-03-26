package com.base.rpc.processor;

import com.base.rpc.Instantiate.InstantiateImpl;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RPCProviderProcessor implements Processor<BaseProtocol> {

    private final ExecutorService pool = Executors.newCachedThreadPool();


    @Override
    public void process(AioSession session, BaseProtocol msg) {

        pool.execute(
                ()->{
                    BaseProtocol.Builder response = msg.toBuilder();
                    response.setReqRes(2);
                    try {
                        InstantiateImpl instantiate = new InstantiateImpl(msg);
                        instantiate.invoke();
                        synchronized (session){
                            byte[] data = instantiate.getBytes();
                            session.writeBuffer().writeInt(data.length + 4);
                            session.writeBuffer().write(data);
                            session.writeBuffer().flush();
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

}
