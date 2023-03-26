package com.base.rpc.Instantiate;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * 实例化
 *
 * @author bai
 * @date 2023/03/26
 */
public interface Instantiate<T> {


    /**
     * 调用
     *
     * @param t t
     * @return {@link Object}
     */
    Instantiate<T> invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException;
}
