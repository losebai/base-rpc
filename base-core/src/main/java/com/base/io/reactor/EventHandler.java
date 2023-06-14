package com.base.io.reactor;

import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static com.base.io.reactor.Config.BUFFER_SIZE;

@Getter
public class EventHandler implements Handler{

    private final SocketChannel channel;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer writeBuffer;
    private int bytesRead;

    private String host;

    public EventHandler(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void process(byte[] bytes) {
        writeBuffer = ByteBuffer.wrap(bytes);
    }


    public int read(ByteBuffer buffer) throws IOException {
        bytesRead = channel.read(buffer);
        return bytesRead;
    }

    public int write() throws IOException {
        return channel.write(writeBuffer);
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

    public String getHost() {
        try {
            return channel.getLocalAddress().toString();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
