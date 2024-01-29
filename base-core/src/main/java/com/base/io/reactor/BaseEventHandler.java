package com.base.io.reactor;

import com.base.io.common.EventHandler;
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
public abstract class BaseEventHandler<T> implements EventHandler<T> {

    private final Map<String, CompletableFuture<BaseProtocol>> syncRespMap = new ConcurrentHashMap<>();

    public BaseEventHandler()  {}

    public abstract T process(T t);

    public Map<String, CompletableFuture<BaseProtocol>> getSyncRespMap() {
        return syncRespMap;
    }

    public void onEvent(EventType type, ByteBuffer buffer) throws IOException {
        switch (type) {
            case READ:
                break;
            case WRITE:
                break;
        }
    }

    /**
     * 可读回调
     */
    @Override
    public void readable(ByteBuffer buffer){

    }


    /**
     * 可写回调
     */
    @Override
    public void writeable(){


    }

}
