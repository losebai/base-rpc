/*******************************************************************************
 * Copyright (c) 2017-2019, org.smartboot. All rights reserved.
 * project name: smart-socket
 * file name: SslService.java
 * Date: 2019-12-31
 * Author: sandao (zhengjunweimail@163.com)
 *
 ******************************************************************************/

package org.smartboot.socket.extension.ssl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.socket.buffer.BufferPage;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.Consumer;

/**
 * TLS/SSL服务
 * keytool -genkey -validity 36000 -alias www.smartboot.org -keyalg RSA -keystore server.keystore
 *
 * @author 三刀
 * @version V1.0 , 2018/1/1
 */
public final class SslService {

    private static final Logger logger = LoggerFactory.getLogger(SslService.class);

    private final SSLContext sslContext;

    private final Consumer<SSLEngine> consumer;
    private final CompletionHandler<Integer, HandshakeModel> handshakeCompletionHandler = new CompletionHandler<Integer, HandshakeModel>() {
        @Override
        public void completed(Integer result, HandshakeModel attachment) {
            if (result == -1) {
                attachment.setEof(true);
            }
            synchronized (attachment) {
                doHandshake(attachment);
            }
        }

        @Override
        public void failed(Throwable exc, HandshakeModel attachment) {
            attachment.setEof(true);
            attachment.getHandshakeCallback().callback();
        }
    };

    public SslService(SSLContext sslContext, Consumer<SSLEngine> consumer) {
        this.sslContext = sslContext;
        this.consumer = consumer;
    }

    HandshakeModel createSSLEngine(AsynchronousSocketChannel socketChannel, BufferPage bufferPage) {
        try {
            HandshakeModel handshakeModel = new HandshakeModel();
            SSLEngine sslEngine = sslContext.createSSLEngine();
            SSLSession session = sslEngine.getSession();

            //更新SSLEngine配置
            consumer.accept(sslEngine);

            handshakeModel.setSslEngine(sslEngine);
            handshakeModel.setAppWriteBuffer(bufferPage.allocate(session.getApplicationBufferSize()));
            handshakeModel.setNetWriteBuffer(bufferPage.allocate(session.getPacketBufferSize()));
            handshakeModel.getNetWriteBuffer().buffer().flip();
            handshakeModel.setAppReadBuffer(bufferPage.allocate(session.getApplicationBufferSize()));
            handshakeModel.setNetReadBuffer(bufferPage.allocate(session.getPacketBufferSize()));
            sslEngine.beginHandshake();

            handshakeModel.setSocketChannel(socketChannel);
            return handshakeModel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 纯异步实现的SSL握手,
     * 在执行doHandshake期间必须保证当前通道无数据读写正在执行。
     * 若触发了数据读写，也应立马终止doHandshake方法
     *
     * @param handshakeModel
     */
    public void doHandshake(HandshakeModel handshakeModel) {
        SSLEngineResult result = null;
        try {
            SSLEngineResult.HandshakeStatus handshakeStatus = null;
            ByteBuffer netReadBuffer = handshakeModel.getNetReadBuffer().buffer();
            ByteBuffer appReadBuffer = handshakeModel.getAppReadBuffer().buffer();
            ByteBuffer netWriteBuffer = handshakeModel.getNetWriteBuffer().buffer();
            ByteBuffer appWriteBuffer = handshakeModel.getAppWriteBuffer().buffer();
            SSLEngine engine = handshakeModel.getSslEngine();

            //握手阶段网络断链
            if (handshakeModel.isEof()) {
                logger.info("the ssl handshake is terminated");
                handshakeModel.getHandshakeCallback().callback();
                return;
            }
            while (!handshakeModel.isFinished()) {
                handshakeStatus = engine.getHandshakeStatus();
                if (logger.isDebugEnabled()) {
                    logger.info("握手状态:" + handshakeStatus);
                }
                switch (handshakeStatus) {
                    case NEED_UNWRAP:
                        //解码
                        netReadBuffer.flip();
                        if (netReadBuffer.hasRemaining()) {
                            result = engine.unwrap(netReadBuffer, appReadBuffer);
                            netReadBuffer.compact();
                        } else {
                            netReadBuffer.clear();
                            handshakeModel.getSocketChannel().read(netReadBuffer, handshakeModel, handshakeCompletionHandler);
                            return;
                        }

                        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                            handshakeModel.setFinished(true);
                            netReadBuffer.clear();
                        }
                        switch (result.getStatus()) {
                            case OK:
                                break;
                            case BUFFER_OVERFLOW:
                                logger.warn("doHandshake BUFFER_OVERFLOW");
                                break;
                            //两种情况会触发BUFFER_UNDERFLOW,1:读到的数据不够,2:netReadBuffer空间太小
                            case BUFFER_UNDERFLOW:
                                logger.warn("doHandshake BUFFER_UNDERFLOW");
                                return;
                            default:
                                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                        }
                        break;
                    case NEED_WRAP:
                        if (netWriteBuffer.hasRemaining()) {
                            logger.info("数据未输出完毕...");
                            handshakeModel.getSocketChannel().write(netWriteBuffer, handshakeModel, handshakeCompletionHandler);
                            return;
                        }
                        netWriteBuffer.clear();
                        result = engine.wrap(appWriteBuffer, netWriteBuffer);
                        switch (result.getStatus()) {
                            case OK:
                                appWriteBuffer.clear();
                                netWriteBuffer.flip();
                                if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
                                    handshakeModel.setFinished(true);
                                }
                                handshakeModel.getSocketChannel().write(netWriteBuffer, handshakeModel, handshakeCompletionHandler);
                                return;
                            case BUFFER_OVERFLOW:
                                logger.warn("NEED_WRAP BUFFER_OVERFLOW");
                                break;
                            case BUFFER_UNDERFLOW:
                                throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                            case CLOSED:
                                logger.warn("closed");
                                try {
                                    netWriteBuffer.flip();
                                    netReadBuffer.clear();
                                } catch (Exception e) {
                                    logger.warn("Failed to send server's CLOSE message due to socket channel's failure.");
                                }
                                break;
                            default:
                                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                        }
                        break;
                    case NEED_TASK:
                        Runnable task;
                        while ((task = engine.getDelegatedTask()) != null) {
                            task.run();
                        }
                        break;
                    case FINISHED:
                        logger.info("HandshakeFinished");
                        break;
                    case NOT_HANDSHAKING:
                        logger.error("NOT_HANDSHAKING");
                        break;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("握手完毕");
            }
            handshakeModel.getHandshakeCallback().callback();

        } catch (Exception e) {
            logger.warn("ignore doHandshake exception: {}", e.getMessage());
            handshakeModel.setEof(true);
            handshakeModel.getHandshakeCallback().callback();
        }
    }

}
