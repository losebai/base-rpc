package com.item.test.http;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;

import java.io.IOException;


public class HttpBootstrapTest {

    public static void main(String[] args) {
        HttpBootstrap bootstrap = new HttpBootstrap().httpHandler(new HttpServerHandler() {
            final byte[] bytes = "hello world".getBytes();

            @Override
            public void handle(HttpRequest request, HttpResponse response) throws IOException {
                response.setContentLength(bytes.length);
                response.setContentType("text/plain; charset=UTF-8");
                response.write(bytes);
            }
        }).setPort(8080);
        bootstrap.configuration()
                .threadNum(Runtime.getRuntime().availableProcessors())
                .readBufferSize(1024 * 4)
                .writeBufferSize(1024 * 4)
                .readMemoryPool(16384 * 1024 * 4)
                .writeMemoryPool(10 * 1024 * 1024 * Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors()).debug(false);
        bootstrap.start();
    }
}