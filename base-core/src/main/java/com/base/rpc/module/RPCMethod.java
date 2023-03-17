package com.base.rpc.module;


import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

/**
 * rpcmethod
 *
 * @author bai
 * @date 2023/03/17
 */
@Data
public class RPCMethod implements Serializable {

    /**
     * 名称空间
     */
    private String namespace;

    /**
     * 方法名称
     */
    private String methodName;


    /**
     * 参数个数
     */
    private String[] params;


    /**
     * 结果类型
     */
    private Class<?> resultType;

    /**
     * 方法大小
     */
    private Integer methodLength;

    /**
     * 请求回调函数
     */
    private Function<?, ?> requestFunc;

    /**
     * 响应回调函数
     */
    private Function<?, ?> responseFunc;

    /**
     * 构造函数
     */
    private Function<?, ?> constructorFunc;

    @Override
    public String toString() {
        return "RPCMethod{" +
                "namespace='" + namespace + '\'' +
                ", methodName='" + methodName + '\'' +
                ", params=" + Arrays.toString(params) +
                ", resultType=" + resultType +
                ", methodLength=" + methodLength +
                ", requestFunc=" + requestFunc +
                ", responseFunc=" + responseFunc +
                ", constructorFunc=" + constructorFunc +
                '}';
    }
}