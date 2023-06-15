package com.base.io.reactor;

import com.base.core.util.ThreadPoolUtil;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import static com.base.io.reactor.Config.BUFFER_SIZE;

@Getter
public class EventHandler implements Handler{

    private final SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer writeBuffer;
    private int bytesRead;

    private static final ExecutorService workPool = ThreadPoolUtil.newThreadPool();

    private final String host;

    public EventHandler(SocketChannel channel) throws IOException {
        this.channel = channel;
        host = channel.getRemoteAddress().toString();
    }

    @Override
    public void process(byte[] bytes) {
        workPool.submit(()->{
            writeBuffer = ByteBuffer.wrap(bytes);
        });
    }


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
