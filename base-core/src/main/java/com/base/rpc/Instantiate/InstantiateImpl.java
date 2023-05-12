package com.base.rpc.Instantiate;

import com.base.core.util.ByteToUtil;
import com.base.core.util.ClassLoaderMapperUtil;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 实例化impl
 *
 * @author bai
 * @date 2023/03/28
 */
public class InstantiateImpl implements Instantiate<BaseProtocol> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateImpl.class);

    private final BaseProtocol.Builder baseProtocol;

    private final BaseProtocol.Body.Builder bodyBuilder;

    private  BaseProtocol.Body body;

    private final Class<?> implClass;

    private byte[] bytes = null;

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

    public Instantiate<BaseProtocol> invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException {
        LOGGER.info("Instantiate " +  baseProtocol.getRequestID().toStringUtf8());
        ByteString[] paramClassList = this.bodyBuilder.getParamsTypeList().toArray(new ByteString[0]);
        ByteString[]  paramObjList = this.bodyBuilder.getParamsObjList().toArray(new ByteString[0]);
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
            throw new RuntimeException(e);
        }

        // 调用接口
        Object impObj = implClass.getDeclaredConstructor().newInstance();
        Method method = impObj.getClass().getMethod(bodyBuilder.getMethodName().toStringUtf8(), classArray);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            Object obj = method.invoke(impObj, args); // run
            oos.writeObject(obj);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            bodyBuilder.setMethodLength(bytes.length);
            this.body = bodyBuilder.build();
            this.bytes = body.toByteArray();
            bodyBuilder.setReturn(ByteString.copyFrom(bytes));
            baseProtocol.setDataLength(this.bytes.length);
            bodyBuilder.setResultType(ByteString.copyFromUtf8(method.getReturnType().getName()));
            this.body = bodyBuilder.build();
            baseProtocol.setBody(body);
            this.bytes = baseProtocol.build().toByteArray();
        } catch (IOException e) {
            this.bodyBuilder.setException(ByteString.copyFromUtf8(e.getMessage()));
        } finally {
            oos.close();
            baos.close();
        }
        return this;
    }

    public BaseProtocol.Body getBody(){
        return this.body;
    }

    public BaseProtocol getBaseProtocol(){
        return this.baseProtocol.buildPartial();
    }

    public byte[] getBytes(){
        return this.bytes;
    }
}

