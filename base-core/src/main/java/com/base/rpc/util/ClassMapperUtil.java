package com.base.rpc.util;

import java.util.HashMap;
import java.util.Map;


/**
 * 类映射器跑龙套
 *
 * @author bai
 * @date 2023/03/20
 */
public class ClassMapperUtil {

    /**
     * 基础数据类型
     */
    private static final Map<String, Class<?>> primitiveClass = new HashMap<>();
    static
    {
        primitiveClass.put("int", int.class);
        primitiveClass.put("double", double.class);
        primitiveClass.put("long", long.class);
    }


    public static Class<?> getClass(String key){

        return primitiveClass.get(key);
    }
}
