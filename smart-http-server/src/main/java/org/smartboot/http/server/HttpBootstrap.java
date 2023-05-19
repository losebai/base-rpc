/*******************************************************************************
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: HttpBootstrap.java
 * Date: 2021-02-07
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.http.server;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HeaderValueEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpProtocolEnum;
import org.smartboot.http.server.impl.HttpMessageProcessor;
import org.smartboot.http.server.impl.HttpRequestProtocol;
import org.smartboot.socket.buffer.BufferPagePool;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class HttpBootstrap {

    private static final String BANNER = "* **************************************************************************\n" +
            "     * ********************                                  ********************\n" +
            "     * ********************      COPYRIGHT INFORMATION       ********************\n" +
            "     * ********************                                  ********************\n" +
            "     * **************************************************************************\n" +
            "     *                                                                          *\n" +
            "     *                                   _oo8oo_                                *\n" +
            "     *                                  o8888888o                               *\n" +
            "     *                                  88\" . \"88                               *\n" +
            "     *                                  (| -_- |)                               *\n" +
            "     *                                  0\\  =  /0                               *\n" +
            "     *                                ___/'==='\\___                             *\n" +
            "     *                              .' \\\\|     |// '.                           *\n" +
            "     *                             / \\\\|||  :  |||// \\                          *\n" +
            "     *                            / _||||| -:- |||||_ \\                         *\n" +
            "     *                           |   | \\\\\\  -  /// |   |                        *\n" +
            "     *                           | \\_|  ''\\---/''  |_/ |                        *\n" +
            "     *                           \\  .-\\__  '-'  __/-.  /                        *\n" +
            "     *                         ___'. .'  /--.--\\  '. .'___                      *\n" +
            "     *                      .\"\" '<  '.___\\_<|>_/___.'  >' \"\".                   *\n" +
            "     *                     | | :  `- \\`.:`\\ _ /`:.`/ -`  : | |                  *\n" +
            "     *                     \\  \\ `-.   \\_ __\\ /__ _/   .-` /  /                  *\n" +
            "     *                 =====`-.____`.___ \\_____/ ___.`____.-`=====              *\n" +
            "     *                                   `=---=`                                *\n" +
            "     * **************************************************************************\n" +
            "     * ********************                                  ********************\n" +
            "     * ********************      \t\t\t\t ********************\n" +
            "     * ********************         佛祖保佑 永远无BUG       ********************\n" +
            "     * ********************                                  ********************\n" +
            "     * **************************************************************************";

    private static final String VERSION = "1.2.1";
    /**
     * http消息解码器
     */
    private final HttpMessageProcessor processor;
    private final HttpServerConfiguration configuration = new HttpServerConfiguration();
    private AioQuickServer server;
    /**
     * Http服务端口号
     */
    private int port = 8080;


    public HttpBootstrap() {
        this(new HttpMessageProcessor());
    }

    public HttpBootstrap(HttpMessageProcessor processor) {
        this.processor = processor;
        this.processor.setConfiguration(configuration);
    }

    /**
     * Http服务端口号
     */
    public HttpBootstrap setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * 往 http 处理器管道中注册 Handle
     *
     * @param httpHandler
     * @return
     */
    public HttpBootstrap httpHandler(HttpServerHandler httpHandler) {
        processor.httpServerHandler(httpHandler);
        return this;
    }

    /**
     * 获取websocket的处理器管道
     *
     * @return
     */
    public HttpBootstrap webSocketHandler(WebSocketHandler webSocketHandler) {
        processor.setWebSocketHandler(webSocketHandler);
        return this;
    }

    /**
     * 服务配置
     *
     * @return
     */
    public HttpServerConfiguration configuration() {
        return configuration;
    }

    /**
     * 启动HTTP服务
     *
     * @throws RuntimeException
     */
    public void start() {
        initByteCache();
        BufferPagePool readBufferPool = new BufferPagePool(configuration.getReadPageSize(), 1, false);
        configuration.getPlugins().forEach(processor::addPlugin);

        server = new AioQuickServer(configuration.getHost(), port, new HttpRequestProtocol(configuration), processor);
        server.setThreadNum(configuration.getThreadNum())
                .setBannerEnabled(false)
                .setBufferFactory(() -> new BufferPagePool(configuration.getWritePageSize(), configuration.getWritePageNum(), true))
                .setReadBufferFactory(bufferPage -> readBufferPool.allocateBufferPage().allocate(configuration.getReadBufferSize()))
                .setWriteBuffer(configuration.getWriteBufferSize(), 16);
        try {
            if (configuration.isBannerEnabled()) {
                System.out.println(BANNER + "\r\n :: smart-http :: (" + VERSION + ")");
            }
            if (configuration.group() == null) {
                server.start(configuration.group());
            } else {
                server.start(configuration.group());
            }

        } catch (IOException e) {
            throw new RuntimeException("server start error.", e);
        }
    }

    private void updateHeaderNameByteTree() {
        configuration.getHeaderNameByteTree().addNode(HeaderNameEnum.UPGRADE.getName(), upgrade -> {
            // WebSocket
            if (HeaderValueEnum.WEBSOCKET.getName().equals(upgrade)) {
                return configuration.getWebSocketHandler();
            }
            // HTTP/2.0
            else if (HeaderValueEnum.H2C.getName().equals(upgrade) || HeaderValueEnum.H2.getName().equals(upgrade)) {
                return new Http2ServerHandler() {
                    @Override
                    public void handle(HttpRequest request, HttpResponse response) throws IOException {
                        configuration.getHttpServerHandler().handle(request, response);
                    }

                    @Override
                    public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws IOException {
                        configuration.getHttpServerHandler().handle(request, response, completableFuture);
                    }
                };
            } else {
                return null;
            }
        });
    }

    private void initByteCache() {
        for (HttpMethodEnum httpMethodEnum : HttpMethodEnum.values()) {
            configuration.getByteCache().addNode(httpMethodEnum.getMethod());
        }
        for (HttpProtocolEnum httpProtocolEnum : HttpProtocolEnum.values()) {
            configuration.getByteCache().addNode(httpProtocolEnum.getProtocol());
        }
        for (HeaderNameEnum headerNameEnum : HeaderNameEnum.values()) {
            configuration.getHeaderNameByteTree().addNode(headerNameEnum.getName());
        }
        for (HeaderValueEnum headerValueEnum : HeaderValueEnum.values()) {
            configuration.getByteCache().addNode(headerValueEnum.getName());
        }

        updateHeaderNameByteTree();
    }

    /**
     * 停止服务
     */
    public void shutdown() {
        if (server != null) {
            server.shutdown();
            server = null;
        }
    }
}
