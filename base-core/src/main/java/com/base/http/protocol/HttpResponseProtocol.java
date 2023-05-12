package com.base.http.protocol;

import com.base.core.util.ByteToUtil;
import com.base.http.module.HttpResponse;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class HttpResponseProtocol implements Protocol<HttpResponse> {


    @Override
    public HttpResponse decode(ByteBuffer readBuffer, AioSession session) {

        int messageSize = getMessage(readBuffer);
        if ( messageSize == -1){
            return null;
        }
        log.info("HttpResponseProtocol  parse init ....");
        byte[] data = new byte[messageSize - INTEGER_BYTES];
        readBuffer.getInt();
        readBuffer.get(data);

        try {
            Object obj = ByteToUtil.deserialize(data);
            return (HttpResponse)obj;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
