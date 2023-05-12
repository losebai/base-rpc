package com.base.http.processor;

import com.base.http.module.HttpResponse;
import com.base.core.processor.Processor;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class HttpResponseProcessor implements Processor<HttpResponse> {


    @Override
    public void process(AioSession session, HttpResponse msg) {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setDate(LocalDateTime.now().toString());
        String body = "HTTP/1.1 200\n" +
                "Set-Cookie: JSESSIONID=d61e031e-bfc5-40ff-b3b2-2b11a41f9909; Path=/tool-admin; HttpOnly; SameSite=lax\n" +
                "Set-Cookie: JSESSIONID=72b52422-b151-431b-835d-51d886ee1443; Path=/tool-admin; HttpOnly; SameSite=lax\n" +
                "Vary: Origin\n" +
                "Vary: Access-Control-Request-Method\n" +
                "Vary: Access-Control-Request-Headers\n" +
                "Access-Control-Allow-Origin: http://192.168.3.43:8001\n" +
                "Access-Control-Allow-Credentials: true\n" +
                "Content-Type: application/json\n" +
                "Transfer-Encoding: chunked\n" +
                "Date: Fri, 12 May 2023 07:30:15 GMT\n" +
                "Keep-Alive: timeout=30\n" +
                "Connection: keep-alive";
        try {
            WriteBuffer writeBuffer = session.writeBuffer();
            writeBuffer.writeInt(body.getBytes().length);
            writeBuffer.write(body.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
