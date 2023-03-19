package com.base.rpc.processor;

import com.base.rpc.module.Response;
import com.base.rpc.processor.Processor;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.base.rpc.util.ClassMapperUtil;
import com.google.protobuf.Any;
import org.smartboot.socket.transport.AioSession;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RPCProviderProcessor implements Processor<BaseProtocol> {

    private final ExecutorService pool = Executors.newCachedThreadPool();

    private final Map<String, Object> impMap = new HashMap<String, Object>();

    @Override
    public void process(AioSession session, BaseProtocol msg) {

        pool.execute(
                ()->{
                    BaseProtocol.Builder response = msg.toBuilder();
                    response.setReqRes(2);

                    BaseProtocol.Body.Builder method = BaseProtocol.Body.newBuilder();

                    BaseProtocol.Body req = msg.getBody();
                    String[] paramClassList = req.getParamsTypeList().toArray(new String[0]);
                    List<Any> paramObjList = req.getParamsObjList();

                    // 获取入参类型
                    Class<?>[] classArray = null;
                    classArray = new Class[paramClassList.length];

                    try {
                        for (int i = 0; i < classArray.length; i++) {
                            Class<?> clazz = ClassMapperUtil.getClass(paramClassList[i]);
                            if (clazz == null) {
                                classArray[i] = Class.forName(paramClassList[i]);
                            } else {
                                classArray[i] = clazz;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    // 调用接口
                    Object impObj = impMap.get(req.getInterfaceClass());
                    if (impObj == null) {
                        throw new UnsupportedOperationException("can not find interface: " + req.getInterfaceClass());
                    }
                    Method method = impObj.getClass().getMethod(req.getMethod(), classArray);
                    Object obj = method.invoke(impObj, paramObjList); // run
                    resp.setReturnObject(obj); // return
                    resp.setReturnType(method.getReturnType().getName()); // return type

                }
        );
    }


    public final <T> void publishService(Class<T> apiName, T apiImpl) {
        impMap.put(apiName.getName(), apiImpl);
    }

}
