package com.base.core.entity;

import lombok.Data;

import java.io.Serializable;


/**
 * 本地方法dto
 *
 * @author bai
 * @date 2023/06/19
 */
@Data
public class LocalMethodDto implements Serializable {

    /**
     * 协议
     */
    String protocol;

    /**
     * 版本
     */
    Integer version;

    /**
     * 方法名称
     */
    String methodName;

    /**
     * 参数个数
     */
    Object[] params;

    /**
     * 参数类型
     */
    String[] paramsType;

    /**
     * 返回类型
     */
    String returnType;

    /**
     * 返回obj
     */
    Object returnObj;
}
