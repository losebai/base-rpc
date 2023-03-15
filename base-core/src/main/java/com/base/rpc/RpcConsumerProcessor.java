package com.base.rpc;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class RpcConsumerProcessor implements MessageProcessor<byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcConsumerProcessor.class);
    private final Map<String, CompletableFuture<Response>> synchRespMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> objectMap = new ConcurrentHashMap<>();
    private AioSession aioSession;

    @Override
    public void process(AioSession session, byte[] msg) {
        ObjectInput objectInput = null;
        try {
            objectInput = new ObjectInputStream(new ByteArrayInputStream(msg));
            Response resp = (Response) objectInput.readObject();
            synchRespMap.get(resp.getUuid()).complete(resp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectInput != null) {
                try {
                    objectInput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public <T> T getObject(final Class<T> remoteInterface) {
        Object obj = objectMap.get(remoteInterface);
        if (obj != null) {
            return (T) obj;
        }
        obj = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{remoteInterface},
                (proxy, method, args) -> {
                    Request req = new Request();
                    req.setInterfaceClass(remoteInterface.getName());
                    req.setMethod(method.getName());
                    Class<?>[] types = method.getParameterTypes();
                    if (!ArrayUtils.isEmpty(types)) {
                        String[] paramClass = new String[types.length];
                        for (int i = 0; i < types.length; i++) {
                            paramClass[i] = types[i].getName();
                        }
                        req.setParamClassList(paramClass);
                    }
                    req.setParams(args);

                    Response rmiResp = sendRpcRequest(req);
                    if (StringUtils.isNotBlank(rmiResp.getException())) {
                        throw new RuntimeException(rmiResp.getException());
                    }
                    return rmiResp.getReturnObject();
                });
        objectMap.put(remoteInterface, obj);
        return (T) obj;
    }

    private Response sendRpcRequest(Request request) throws Exception {
        CompletableFuture<Response> rpcResponseCompletableFuture = new CompletableFuture<>();
        synchRespMap.put(request.getUuid(), rpcResponseCompletableFuture);

        //输出消息
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = new ObjectOutputStream(byteArrayOutputStream);
        objectOutput.writeObject(request);
        byte[] data=byteArrayOutputStream.toByteArray();
        synchronized (aioSession) {
            aioSession.writeBuffer().writeInt(data.length + 4);
            aioSession.writeBuffer().write(data);
            aioSession.writeBuffer().flush();
        }
//        aioSession.write(byteArrayOutputStream.toByteArray());
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
