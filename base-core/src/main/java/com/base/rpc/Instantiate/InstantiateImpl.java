package com.base.rpc.Instantiate;

import com.base.core.util.ByteToUtil;
import com.base.core.util.ClassLoaderMapperUtil;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 实例化impl , 阮如协议中的数据，进行解析运行结果
 *
 * @author bai
 * @date 2023/03/28
 */
public class InstantiateImpl implements Instantiate<BaseProtocol> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateImpl.class);

    private final BaseProtocol.Builder baseProtocol;

    private final BaseProtocol.Body.Builder bodyBuilder;

    private final Class<?> implClass;


    public InstantiateImpl(BaseProtocol baseProtocol, Class<?> implClass){
        this.baseProtocol = baseProtocol.toBuilder();
        this.bodyBuilder = baseProtocol.getBody().toBuilder();
        this.implClass = implClass;
        try {
            ClassLoaderMapperUtil.addClass(bodyBuilder.getClassName().toStringUtf8());
        } catch (ClassNotFoundException e){
            this.bodyBuilder.setException(ByteString.copyFromUtf8(e.getException().getMessage()));
        }
    }

    public Object invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException {
        LOGGER.info("Instantiate " +  baseProtocol.getRequestID().toStringUtf8());
        ByteString[] paramClassList = this.bodyBuilder.getParamsTypeList().toArray(new ByteString[0]);
        ByteString[] paramObjList = this.bodyBuilder.getParamsObjList().toArray(new ByteString[0]);
        Object[] args = new Object[paramObjList.length];
        // 获取入参类型
        Class<?>[] classArray = null;
        classArray = new Class[paramClassList.length];

        try {
            for (int i = 0; i < classArray.length; i++) {
                Class<?> clazz = ClassLoaderMapperUtil.getClass(paramClassList[i].toStringUtf8());
                if (clazz == null) {
                    classArray[i] = Class.forName(paramClassList[i].toStringUtf8());
                } else {
                    classArray[i] = clazz;
                }
                args[i] = ByteToUtil.deserialize(paramObjList[i].toByteArray());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // 调用接口
        Object impObj = implClass.getDeclaredConstructor().newInstance();
        Method method = impObj.getClass().getMethod(bodyBuilder.getMethodName().toStringUtf8(), classArray);
        return method.invoke(impObj, args); // run

    }

    public BaseProtocol getBaseProtocol(){
        return this.baseProtocol.buildPartial();
    }

}
