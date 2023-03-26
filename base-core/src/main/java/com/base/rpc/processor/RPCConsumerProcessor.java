package com.base.rpc.processor;

import com.base.core.util.ByteStringUtil;
import com.base.rpc.Protocol.RpcInvocationHandler;
import com.base.rpc.Protocol.RpcProxyInvocationHandler;
import com.base.rpc.module.Request;
import com.base.rpc.module.Response;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class RPCConsumerProcessor implements Processor<BaseProtocol> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCConsumerProcessor.class);
    private final Map<ByteString, CompletableFuture<BaseProtocol.Body>> syncRespMap = new ConcurrentHashMap<>();
    private AioSession aioSession;

    @Override
    public void process(AioSession session, BaseProtocol msg) {
        syncRespMap.get(msg.getRequestID()).complete(msg.getBody());
        System.out.println("处理消息");
    }

    public <T> T getObject(final Class<T> remoteInterface) throws Exception {

        T classObj = remoteInterface.getDeclaredConstructor().newInstance();
        RpcProxyInvocationHandler<T> rpcProxyInvocationHandler = new RpcInvocationHandler<>(classObj);
        Object obj = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{remoteInterface},
                rpcProxyInvocationHandler);

        BaseProtocol.Builder builder =  BaseProtocol.newBuilder();
        builder.setReqRes(1);
        builder.setEvent(1);
        builder.setMagicHigh(ByteStringUtil.ByteStringConst.magicHigh);
        builder.setRequestID(ByteString.copyFromUtf8(UUID.randomUUID().toString()));
        BaseProtocol baseProtocol = builder.build();
        sendRpcRequest(baseProtocol);
        return (T) obj;
    }

    private BaseProtocol.Body sendRpcRequest(BaseProtocol request) throws Exception {
        CompletableFuture<BaseProtocol.Body> rpcResponseCompletableFuture = new CompletableFuture<>();
        syncRespMap.put(request.getRequestID(), rpcResponseCompletableFuture);

        //输出消息
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
        objectOutput.writeObject(request);
        byte[] data = byteArrayOutputStream.toByteArray();
        synchronized (aioSession) {
            aioSession.writeBuffer().writeInt(data.length + 4);
            aioSession.writeBuffer().write(data);
            aioSession.writeBuffer().flush();
        }
        try {
            return rpcResponseCompletableFuture.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new SocketTimeoutException("Message is timeout!");
        }
    }

    @Override
    public void stateEvent(AioSession session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                this.aioSession = session;
                break;
        }
    }

}
