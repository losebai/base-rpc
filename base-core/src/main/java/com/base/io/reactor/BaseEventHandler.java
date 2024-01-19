package com.base.io.reactor;

import com.base.rpc.protocol.RPCProtocol.BaseProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 事件处理程序
 *
 * @author bai
 * @date 2023/08/11
 */
public abstract class BaseEventHandler<T>{

    private final Map<String, CompletableFuture<BaseProtocol>> syncRespMap = new ConcurrentHashMap<>();

    public BaseEventHandler()  {
    }

    public abstract void process(T t);

    public abstract int read(ByteBuffer buffer) throws IOException;

    public abstract int write(ByteBuffer buffer) throws IOException;

    public abstract void flush() throws IOException;

    public Map<String, CompletableFuture<BaseProtocol>> getSyncRespMap() {
        return syncRespMap;
    }

    public void onEvent(EventType type, ByteBuffer buffer) throws IOException {
        switch (type) {
            case READ:
                read(buffer);
                break;
            case WRITE:
                write(buffer);
                break;
        }
    }

    /**
     * 可读回调
     */
    public void readable(ByteBuffer buffer){

    }


    /**
     * 可写回调
     */
    public void writeable(){


    }

}
