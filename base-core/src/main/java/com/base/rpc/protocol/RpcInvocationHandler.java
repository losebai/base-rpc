package com.base.rpc.protocol;

import com.base.core.util.ByteToUtil;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * rpc调用处理程序
 *
 * @author bai
 * @date 2023/03/26
 */
public class RpcInvocationHandler<T> implements RpcProxyInvocationHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcInvocationHandler.class);
    private final BaseProtocol.Body.Builder bodyBuilder = BaseProtocol.Body.newBuilder();

    private T target;

    private ProxyFunction<?> beforeFunc;


    private ProxyFunction<?> afterFunc;

    public RpcInvocationHandler(){
    }

    public interface ProxyFunction<R>{

        /**
         * 应用
         *
         * @param proxy  代理
         * @param method 方法
         * @param args   arg游戏
         * @return {@link R}
         */
        void apply(Object proxy, Method method, Object[] args) throws Exception;
    }

    public <R> void setBeforeFunc(ProxyFunction<R> beforeFunc) {
        this.beforeFunc = beforeFunc;
    }

    public <R> void setAfterFunc(ProxyFunction<R> afterFunc) {
        this.afterFunc = afterFunc;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (beforeFunc != null){
            beforeFunc.apply(proxy, method, args);
        }
        bodyBuilder.setClassName(ByteString.copyFromUtf8(proxy.getClass().getSimpleName()));
        bodyBuilder.setNamespace(ByteString.EMPTY);
        bodyBuilder.setResultType(ByteString.copyFromUtf8(method.getReturnType().getName()));
        bodyBuilder.setReturn(ByteString.EMPTY);
        bodyBuilder.setMethodName(ByteString.copyFromUtf8(method.getName()));
        Class<?>[] paramsClass =  method.getParameterTypes();
        for(int i =0; i< args.length ; i++){
            bodyBuilder.addParamsType(ByteString.copyFromUtf8(paramsClass[i].getName()));
            bodyBuilder.addParamsObj(ByteString.copyFrom(Objects.requireNonNull(ByteToUtil.streamToBytes(args[i]))));
        }
        this.bodyBuilder.setMethodName(ByteString.copyFromUtf8(method.getName()));
        LOGGER.debug(this.bodyBuilder.toString());
        Class<?>[] types = method.getParameterTypes();
        if (types.length != args.length ){
            throw new RuntimeException("方法和对象不匹配");
        }
        if (types.length > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            try {
                for (int i = 0; i < types.length; i++) {
                    this.bodyBuilder.addParamsType(ByteString.copyFromUtf8(types[i].getName()));
                    oos.writeObject(args[i]);
                    oos.flush();
                    this.bodyBuilder.addParamsObj(ByteString.copyFrom(baos.toByteArray()));
                    baos.reset(); // 重置
                    oos.reset();
                }
            } catch (IOException e){
                this.bodyBuilder.setException(ByteString.copyFromUtf8(e.getMessage()));
                throw new RuntimeException("错误");
            } finally {
                baos.close();
                oos.close();
            }
        }
//        Object obj = method.invoke(target, args);
        if (afterFunc != null){
            afterFunc.apply(proxy, method, args);
        }
        return target;
    }

    public BaseProtocol.Body getBodyBuilder(){
        return bodyBuilder.buildPartial();
    }
}
