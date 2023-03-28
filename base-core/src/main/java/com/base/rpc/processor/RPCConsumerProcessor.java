package com.base.rpc.processor;

import com.base.core.buffer.ImplBuffer;
import com.base.core.util.ByteStringUtil;
import com.base.core.util.ByteToUtil;
import com.base.core.util.ClassLoaderMapperUtil;
import com.base.rpc.Protocol.RpcInvocationHandler;
import com.base.rpc.Protocol.RpcProxyInvocationHandler;
import com.base.rpc.api.DemoApi;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 处理器
 *
 * @author bai
 * @date 2023/03/28
 */
public class RPCConsumerProcessor implements Processor<BaseProtocol> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCConsumerProcessor.class);
    private final Map<ByteString, CompletableFuture<BaseProtocol.Body>> syncRespMap = new ConcurrentHashMap<>();

    private final Map<String, Object> implBuffer = new ImplBuffer<>();

    private AioSession aioSession;

    @Override
    public void process(AioSession session, BaseProtocol msg) {
        syncRespMap.get(msg.getRequestID()).complete(msg.getBody());
        System.out.println("处理消息");
    }

    public <T> T getObject(final Class<T> remoteInterface) throws Exception {
        ClassLoaderMapperUtil.addClass(remoteInterface);
        Object obj = implBuffer.get(remoteInterface.getName());
        if (obj != null) {
            return (T) obj;
        }
        BaseProtocol.Builder builder =  BaseProtocol.newBuilder();
        builder.setReqRes(1);
        builder.setEvent(1);
        builder.setMagicHigh(ByteStringUtil.ByteStringConst.magicHigh);
        builder.setRequestID(ByteString.copyFromUtf8(UUID.randomUUID().toString()));
        BaseProtocol baseProtocol = builder.build();
        BaseProtocol.Body body = sendRpcRequest(baseProtocol);

        // newInstance
        Object returnObj =  ClassLoaderMapperUtil.getClass(remoteInterface.getName()).getDeclaredConstructor().newInstance();

        //proxy
        RpcProxyInvocationHandler<Object> rpcProxyInvocationHandler = new RpcInvocationHandler<>(returnObj);
        obj = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{remoteInterface},
                rpcProxyInvocationHandler);
        implBuffer.put(remoteInterface.getName(), obj);
        return (T) obj;
    }

    private BaseProtocol.Body sendRpcRequest(BaseProtocol request) throws Exception {
        CompletableFuture<BaseProtocol.Body> rpcResponseCompletableFuture = new CompletableFuture<>();
        syncRespMap.put(request.getRequestID(), rpcResponseCompletableFuture);
        rpcResponseCompletableFuture.thenRun(
                ()->{
                    System.out.println("开始执行方法 sendRpcRequest");
                }
        );
        //输出消息
        byte[] data = ByteToUtil.streamToBytes(request);
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
        if (stateMachineEnum == StateMachineEnum.NEW_SESSION) {
            this.aioSession = session;
        }
    }


}
