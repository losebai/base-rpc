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
     * @return {@link Object}
     * @throws NoSuchMethodException     没有这样方法异常
     * @throws InvocationTargetException 调用目标异常
     * @throws IllegalAccessException    非法访问异常
     * @throws IOException               ioexception
     * @throws InstantiationException    实例化异常
     */
    Object invoke() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, InstantiationException;
}
