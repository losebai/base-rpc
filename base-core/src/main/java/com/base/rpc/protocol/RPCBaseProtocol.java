package com.base.rpc.protocol;

import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;


/**
 * rpcbase协议解析
 *
 * @author bai
 * @date 2023/03/19
 */
public class RPCBaseProtocol implements Protocol<BaseProtocol> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCBaseProtocol.class);
    @Override
    public BaseProtocol decode(ByteBuffer readBuffer, AioSession session) {
        int messageSize = getMessage(readBuffer);
        if ( messageSize == -1){
            return null;
        }
        LOGGER.info("RPCBaseProtocol rpc parse init ....");
        byte[] data = new byte[messageSize - INTEGER_BYTES];
        readBuffer.getInt();
        readBuffer.get(data);


        try {
            BaseProtocol baseProtocol = BaseProtocol.parseFrom(data);
            baseProtocol.toBuilder().setDataLength(data.length);
            return baseProtocol;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
