package com.base.rpc.protocol;

import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;


public class ByteProtocol implements Protocol<byte[]> {


    @Override
    public byte[] decode(ByteBuffer readBuffer, AioSession session) {
        int messageSize = getMessage(readBuffer);
        if ( messageSize == -1){
            return null;
        }
        byte[] data = new byte[messageSize - INTEGER_BYTES];
        readBuffer.getInt();
        readBuffer.get(data);
        return data;
    }


}