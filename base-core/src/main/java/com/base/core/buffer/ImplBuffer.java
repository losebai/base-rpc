package com.base.core.buffer;

import java.util.concurrent.ConcurrentHashMap;


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
}
