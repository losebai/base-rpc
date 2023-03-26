package com.base.rpc.Instantiate;

import com.base.core.util.ClassLoaderMapperUtil;
import com.base.rpc.protocol.RPCProtocol.BaseProtocol;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class InstantiateImpl implements Instantiate<BaseProtocol> {

    private final BaseProtocol.Builder baseProtocol;

    private final BaseProtocol.Body.Builder body;

    private byte[] bytes = null;

    private final Object impObj;

    public InstantiateImpl(BaseProtocol baseProtocol, Object impObj){
        this.baseProtocol = baseProtocol.toBuilder();
        this.body = baseProtocol.getBody().toBuilder();
        this.impObj = impObj;
        try {
            ClassLoaderMapperUtil.addClass(body.getClassName().toStringUtf8());
        } catch (ClassNotFoundException e){
            this.body.setException(ByteString.copyFromUtf8(e.getException().getMessage()));
        }
    }

    public Instantiate<BaseProtocol> invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

        String[] paramClassList = this.body.getParamsTypeList().toArray(new String[0]);
        List<Any> paramObjList = this.body.getParamsObjList();

        // 获取入参类型
        Class<?>[] classArray = null;
        classArray = new Class[paramClassList.length];

        try {
            for (int i = 0; i < classArray.length; i++) {
                Class<?> clazz = ClassLoaderMapperUtil.getClass(paramClassList[i]);
                if (clazz == null) {
                    classArray[i] = Class.forName(paramClassList[i]);
                } else {
                    classArray[i] = clazz;
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }


        Method method = impObj.getClass().getMethod(body.getMethodName().toStringUtf8(), classArray);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            Object obj = method.invoke(impObj, paramObjList); // run
            oos.writeObject(obj);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            body.setMethodLength(bytes.length);

            oos.writeObject(baseProtocol);
            oos.flush();
            this.bytes = baos.toByteArray();
            body.setReturn(Any.parseFrom(bytes));
            baseProtocol.setDataLength(bytes.length);
        } catch (IOException e) {
            this.body.setException(ByteString.copyFromUtf8(e.getMessage()));
        } finally {
            oos.close();
            baos.close();
        }
        return this;
    }

    public BaseProtocol.Body getBody(){
        return this.body.buildPartial();
    }

    public BaseProtocol getBaseProtocol(){
        return this.baseProtocol.buildPartial();
    }

    public byte[] getBytes(){
        return this.bytes;
    }
}
