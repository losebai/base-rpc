package com.base.rpc.processor;

import com.base.core.buffer.ImplBuffer;
import com.base.core.util.ByteStringUtil;
import com.base.core.util.ByteToUtil;
import com.base.core.util.ClassLoaderMapperUtil;
import com.base.rpc.Protocol.RpcInvocationHandler;
import com.base.rpc.Protocol.RpcProxyInvocationHandler;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 处理器
 *
 * @author bai
 * @date 2023/03/28
 */
@Slf4j
public class RPCConsumerProcessor implements Processor<BaseProtocol> {

    private final Map<ByteString, CompletableFuture<BaseProtocol.Body>> syncRespMap = new ConcurrentHashMap<>();

    private final Map<String, Object> implBuffer = new ImplBuffer<>();

    private AioSession aioSession;

    @Override
    public void process(AioSession session, BaseProtocol msg) {
        syncRespMap.get(msg.getRequestID()).complete(msg.getBody());
        log.info("client: " +  session.getSessionID()+  "处理消息");
    }

    public <T> T getObject(final Class<T> remoteInterface) throws Exception {
        ClassLoaderMapperUtil.addClass(remoteInterface);
        Object obj = implBuffer.get(remoteInterface.getName());
        if (obj != null) {
            return (T) obj;
        }
        BaseProtocol.Builder builder =  BaseProtocol.newBuilder();
        builder.setReqRes(1);
        builder.setEvent(0);
        builder.setMagicHigh(ByteStringUtil.ByteStringConst.magicHigh);
        builder.setRequestID(ByteString.copyFromUtf8(UUID.randomUUID().toString()));
        builder.setDataLength(0);
        builder.setStatus(BaseProtocol.Status.OK);
        builder.setMagicLow(ByteString.copyFromUtf8("v1"));
        builder.setWay(2);
        BaseProtocol.Body.Builder bodyBuilder = BaseProtocol.Body.newBuilder();
        bodyBuilder.setClassName(ByteString.copyFromUtf8(remoteInterface.getName()));
        bodyBuilder.setNamespace(ByteString.EMPTY);
        bodyBuilder.setMethodLength(0);
        bodyBuilder.setMethodName(ByteString.EMPTY);
        bodyBuilder.setResultType(ByteString.EMPTY);
        bodyBuilder.setReturn(Any.newBuilder().build());
        builder.setBody(bodyBuilder);

        // newInstance
        Object returnObj =  ClassLoaderMapperUtil.getClass(remoteInterface.getName()).getDeclaredConstructor().newInstance();
        RpcProxyInvocationHandler<Object> rpcProxyInvocationHandler = new RpcInvocationHandler<>();
        rpcProxyInvocationHandler.setTarget(returnObj);
        rpcProxyInvocationHandler.setBeforeFunc(
                (p,m,a)->{
                    bodyBuilder.setMethodName(ByteString.copyFromUtf8(m.getName()));
                    BaseProtocol baseProtocol = builder.build();
                    BaseProtocol.Body body = sendRpcRequest(baseProtocol);
                    if (StringUtils.isNotEmpty(body.getException().toStringUtf8())){
                        throw new RuntimeException(body.getException().toStringUtf8());
                    }
                }
        );

        //proxy
        obj = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{remoteInterface},
                rpcProxyInvocationHandler);
        implBuffer.put(remoteInterface.getName(), obj);
        return (T) obj;
    }

    private BaseProtocol.Body sendRpcRequest(BaseProtocol request) throws Exception {
        log.info("client : write data ");
        CompletableFuture<BaseProtocol.Body> rpcResponseCompletableFuture = new CompletableFuture<>();
        syncRespMap.put(request.getRequestID(), rpcResponseCompletableFuture);
        rpcResponseCompletableFuture.thenRun(
                ()->{
                    System.out.println("开始执行方法 sendRpcRequest");
                }
        );
        //输出消息
        byte[] data = request.toByteArray();
        synchronized (aioSession) {
            assert data != null;
            aioSession.writeBuffer().writeInt(data.length + 4);
            aioSession.writeBuffer().write(data);
            aioSession.writeBuffer().flush();
        }
        try {
            return rpcResponseCompletableFuture.get(10, TimeUnit.SECONDS);
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
