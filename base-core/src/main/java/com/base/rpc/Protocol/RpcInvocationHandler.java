package com.base.rpc.Protocol;

import com.base.rpc.Instantiate.InstantiateImpl;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * rpc调用处理程序
 *
 * @author bai
 * @date 2023/03/26
 */
public class RpcInvocationHandler<T> implements RpcProxyInvocationHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcInvocationHandler.class);
    private final BaseProtocol.Body.Builder body = BaseProtocol.Body.newBuilder();

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
        body.clearParamsType();
        body.clearParamsObj();
        body.setMethodName(ByteString.copyFromUtf8(method.getName()));
        LOGGER.debug(body.toString());
        Class<?>[] types = method.getParameterTypes();
        if (types.length != args.length ){
            throw new RuntimeException("方法和对象不匹配");
        }
        if (!ArrayUtils.isEmpty(types)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            try {
                for (int i = 0; i < types.length; i++) {
                    body.addParamsType(ByteString.copyFromUtf8(types[i].getName()));
                    oos.writeObject(args[i]);
                    oos.flush();
                    body.addParamsObj(ByteString.copyFrom(baos.toByteArray()));
                    baos.reset(); // 重置
                    oos.reset();
                }
            } catch (IOException e){
                body.setException(ByteString.copyFromUtf8(e.getMessage()));
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

    public BaseProtocol.Body getBody(){
        return body.buildPartial();
    }
}
