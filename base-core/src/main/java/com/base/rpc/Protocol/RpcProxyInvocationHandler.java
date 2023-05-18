package com.base.rpc.Protocol;

import com.base.rpc.protocol.RPCProtocol.BaseProtocol;

import java.lang.reflect.InvocationHandler;

/**
 * rpc代理调用处理程序
 *
 * @author bai
 * @date 2023/03/26
 */
public interface RpcProxyInvocationHandler<T> extends InvocationHandler {


    <R> void setBeforeFunc(RpcInvocationHandler.ProxyFunction<R> beforeFunc);

    <R> void setAfterFunc(RpcInvocationHandler.ProxyFunction<R> afterFunc);

    void setTarget(T target);

    BaseProtocol.Body getBodyBuilder();
}
