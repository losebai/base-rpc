package com.item.test.nio;

import com.base.core.Protocol.IOBaseProtocol;
import com.base.core.decoder.FixedLengthFrameDecoder;
import com.base.io.common.TCPProcessor;
import com.base.io.reactor.ReactorSocketServer;
import com.base.io.reactor.TCPSession;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 主从分离io
 * 反应器应用
 *
 * @author bai
 * @date 2023/06/15
 */
public class ReactorServer {

    public static class StringIOBaseProtocol implements IOBaseProtocol<String> {

        private final Map<TCPSession, FixedLengthFrameDecoder> decoderMap = new ConcurrentHashMap<>();

        @Override
        public String decode(TCPSession tcpSession, ByteBuffer readBuffer) {
            int remaining = readBuffer.remaining();
            if (remaining < Integer.BYTES) {
                return null;
            }
            readBuffer.mark();
            int length = readBuffer.getInt();
            if (length == 0){
                return null;
            }
            //消息长度超过缓冲区容量引发的半包,启用定长消息解码器,本次解码失败
            if (length + Integer.BYTES > readBuffer.capacity()) {
                FixedLengthFrameDecoder fixedLengthFrameDecoder = new FixedLengthFrameDecoder(length);
                decoderMap.put(tcpSession, fixedLengthFrameDecoder);
                return null;
            }
            //半包，解码失败
            if (length > readBuffer.remaining()) {
                readBuffer.reset();
                return null;
            }
            return convert(readBuffer, length);
        }

        /**
         * 消息解码
         */
        private String convert(ByteBuffer byteBuffer, int length) {
            byte[] b = new byte[length];
            byteBuffer.get(b);
            return new String(b, StandardCharsets.UTF_8);
        }
    }

    public static class StringProcessor implements TCPProcessor<String> {


        @Override
        public void process(TCPSession session, String msg) {
            System.out.println(msg);
            if (msg.equals("你好2")) {
                byte[] bytes = "收到".getBytes();
                ByteBuffer buffer = session.writeBuffer();
                buffer.putInt(bytes.length);
                buffer.put(bytes);
                buffer.flip();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ReactorSocketServer reactorSocketServer = new ReactorSocketServer("127.0.0.1", 7777,
                new StringIOBaseProtocol(), new StringProcessor());
        reactorSocketServer.start();
    }

}
