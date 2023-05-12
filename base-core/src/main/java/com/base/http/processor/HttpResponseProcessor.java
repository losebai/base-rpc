package com.base.http.processor;

import com.base.http.module.HttpResponse;
import com.base.core.processor.Processor;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public class HttpResponseProcessor implements Processor<HttpResponse> {


    @Override
    public void process(AioSession session, HttpResponse msg) {

        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setDate(LocalDateTime.now().toString());

    }
}
