package com.base.core.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class ByteToUtil {


    /**
     * 流字节
     *
     * @param obj obj
     * @return {@link byte[]}
     * @throws IOException ioexception
     */
    public static byte[] streamToBytes(Object obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutput objectOutput = null;
        try {
            objectOutput = new ObjectOutputStream(byteArrayOutputStream);
            objectOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        }catch (IOException ignored){

        }finally {
            try {
                byteArrayOutputStream.close();
                if (objectOutput != null){
                    objectOutput.close();
                }
            }catch (IOException ignored){
            }
        }
        return null;
    }

}
