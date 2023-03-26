package com.base.rpc.Protocol;

import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.lang.reflect.Method;

/**
 * rpc调用处理程序
 *
 * @author bai
 * @date 2023/03/26
 */
public class RpcInvocationHandler<T> implements RpcProxyInvocationHandler<T> {

    private final BaseProtocol.Body.Builder body = BaseProtocol.Body.newBuilder();

    private final T target;

    public RpcInvocationHandler(T target){
        this.target = target;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        body.setMethodName(ByteString.copyFromUtf8(method.getName()));
        Class<?>[] types = method.getParameterTypes();
        if (types.length != args.length ){
            throw new RuntimeException("方法和对象不匹配");
        }
        if (!ArrayUtils.isEmpty(types)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            try {
                for (int i = 0; i < types.length; i++) {
                    body.setParamsType(i, ByteString.copyFromUtf8(types[i].getName()));
                    oos.writeObject(args[i]);
                    oos.flush();
                    body.setParamsObj(i, Any.parseFrom(baos.toByteArray()));
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
        return method.invoke(target, args);
    }

    public BaseProtocol.Body getBody(){
        return body.buildPartial();
    }
}
