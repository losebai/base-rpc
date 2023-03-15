package com.base.rpc;


import lombok.Data;

import java.io.Serializable;
import java.util.UUID;


@Data
public class Request implements Serializable {

    /**
     * 消息的唯一标识
     */
    private final String uuid = UUID.randomUUID().toString();

    /**
     * 接口名称
     */
    private String interfaceClass;

    /**
     * 调用方法
     */
    private String method;

    /**
     * 参数类型字符串
     */
    private String[] paramClassList;

    /**
     * 入参
     */
    private Object[] params;

}
