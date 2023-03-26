package com.base.core.util;

import com.google.protobuf.ByteString;

public class ByteStringUtil {


    public interface ByteStringConst{

        // 标识协议
        ByteString magicHigh = ByteString.copyFromUtf8("rpc");

        //版本号
        ByteString  magicLow =  ByteString.copyFromUtf8("rpc");


        // 仅在 Req/Res 为1（请求）时才有用，标记是否期望从服务器返回值。如果需要来自服务器的返回值，则设置为1。一般应用发送的请求都是1。
        int way = 4;

        // 标识是否是事件消息，例如，心跳事件。如果这是一个事件，则设置为1。
        int event = 5;

        // 标识序列化类型：比如 fastjson 的值为6。
        ByteString serializationID = ByteString.copyFromUtf8("6");


    }
}
