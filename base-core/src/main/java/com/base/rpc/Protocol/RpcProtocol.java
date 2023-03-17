package com.base.rpc.Protocol;

import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;


public class RpcProtocol implements Protocol<byte[]> {
    private static final int INTEGER_BYTES = Integer.SIZE / Byte.SIZE;

    @Override
    public byte[] decode(ByteBuffer readBuffer, AioSession session) {
        int remaining = readBuffer.remaining(); //最多可以读取的数据的位置下标 - 读取的数据的下标；
        if (remaining < INTEGER_BYTES) {
            return null;
        }
        int messageSize = readBuffer.getInt(readBuffer.position());
        if (messageSize > remaining) {
            return null;
        }
        byte[] data = new byte[messageSize - INTEGER_BYTES];
        readBuffer.getInt();
        readBuffer.get(data);
        return data;
    }
}