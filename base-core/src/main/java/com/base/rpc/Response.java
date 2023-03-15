package com.base.rpc;

import lombok.Data;

import java.io.Serializable;


@Data
public class Response implements Serializable {
    /**
     * 消息的唯一标示，与对应的RpcRequest uuid值相同
     */
    private String uuid;
    /**
     * 返回对象
     */
    private Object returnObject;

    /**
     * 返回对象类型
     */
    private String returnType;

    /**
     * 异常
     */
    private String exception;

    public Response(String uuid){
        this.uuid = uuid;
    }

}
