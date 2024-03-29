
package com.base.core.Protocol;

import com.base.core.decoder.FixedLengthFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


/**
 * 字符串协议
 *
 * @author bai
 * @date 2023/05/19
 */
@Slf4j
public class StringProtocol implements Protocol<String> {

    private final Charset charset;

    private final HashMap<AioSession, FixedLengthFrameDecoder> decoderMap = new HashMap<>();
    private long lastClearTime = System.currentTimeMillis();

    public StringProtocol(Charset charset) {
        this.charset = charset;
    }

    public StringProtocol() {
        this(StandardCharsets.UTF_8);
    }

    @Override
    public String decode(ByteBuffer readBuffer, AioSession session) {
        if (System.currentTimeMillis() - lastClearTime > 5000) {
            lastClearTime = System.currentTimeMillis();
            decoderMap.keySet().stream().filter(AioSession::isInvalid).forEach(decoderMap::remove);
        }
        FixedLengthFrameDecoder decoder = decoderMap.get(session);
        //消息长度超过缓冲区容量
        if (decoder != null) {
            String content = bigContent(readBuffer, decoder);
            //解码成功,释放解码器
            if (content != null) {
                decoderMap.remove(session);
            }
            return content;
        }

        int remaining = readBuffer.remaining();
        if (remaining < Integer.BYTES) {
            return null;
        }
        readBuffer.mark();
        int length = readBuffer.getInt();
        //消息长度超过缓冲区容量引发的半包,启用定长消息解码器,本次解码失败
        if (length + Integer.BYTES > readBuffer.capacity()) {
            FixedLengthFrameDecoder fixedLengthFrameDecoder = new FixedLengthFrameDecoder(length);
            decoderMap.put(session, fixedLengthFrameDecoder);
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
     * 大消息体解码
     */
    private String bigContent(ByteBuffer readBuffer, FixedLengthFrameDecoder decoder) {
        if (!decoder.decode(readBuffer)) {
            return null;
        }
        ByteBuffer byteBuffer = decoder.getBuffer();
        return convert(byteBuffer, byteBuffer.capacity());
    }

    /**
     * 消息解码
     */
    private String convert(ByteBuffer byteBuffer, int length) {
        byte[] b = new byte[length];
        byteBuffer.get(b);
        return new String(b, charset);
    }
}
