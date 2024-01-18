package com.base.io.reactor;

import com.base.core.util.ThreadPoolUtil;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static com.base.io.reactor.Config.BUFFER_SIZE;

/**
 * 事件处理程序
 *
 * @author bai
 * @date 2023/08/11
 */
@Getter
public abstract class BaseEventHandler<T>{

    private final SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer writeBuffer;
    private int bytesRead;

    private final Map<String, CompletableFuture<BaseProtocol>> syncRespMap = new ConcurrentHashMap<>();

    private static final ExecutorService workPool = ThreadPoolUtil.newThreadPool();

    private final String host;

    public BaseEventHandler(SocketChannel channel) throws IOException {
        this.channel = channel;
        host = channel.getRemoteAddress().toString();
    }

    public abstract void process(T t);

    public int read(ByteBuffer buffer) throws IOException {
        bytesRead = channel.read(buffer);
        return bytesRead;
    }

    public int write() throws IOException {
        return channel.write(writeBuffer);
    }

    public int write(byte[] bytes) throws IOException {
        return channel.write(ByteBuffer.wrap(bytes));
    }

    public void close() throws IOException {
        channel.close();
    }


    public void onEvent(EventType type) throws IOException {
        switch (type) {
            case READ:
                read(readBuffer);
                break;
            case WRITE:
                write();
                break;
        }
    }
}
