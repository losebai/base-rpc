package com.base.core.buffer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;


/**
 * impl缓冲
 *
 * @author bai
 * @date 2023/03/28
 */
public class ImplBuffer<K,V> extends ConcurrentHashMap<K,V>{

    public ImplBuffer(){
        super(32);
    }

    public V getAndSet(K k, V v){
        if (!contains(k)){
            put(k, v);
        }
        return get(k);
    }

    /**
     * 获取和设置
     * 慢加载
     *
     * @param k        k
     * @param function 函数
     * @return {@link V}
     */
    public V getAndSet(K k, Function<K,V> function){
        if (!contains(k)){
            put(k, function.apply(k));
        }
        return get(k);
    }
}
