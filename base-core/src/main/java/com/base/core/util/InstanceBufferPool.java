package com.base.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实例缓冲池
 *
 * @author bai
 * @date 2023/03/26
 */
public class InstanceBufferPool<T> {

    public final Map<String, T> instanceBufferPool = new ConcurrentHashMap<>(32);


    public T put(String key, T _class) {
        return instanceBufferPool.put(key, _class);
    }


    public T get(String key) {
        return instanceBufferPool.get(key);
    }
}
