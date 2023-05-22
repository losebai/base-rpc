package com.item.test.util;


import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;


/**
 * 原型json跑龙套
 *
 * @author bai
 * @date 2023/03/17
 */
public class ProtoJsonUtils {

    /**
     * tojson
     *
     * @param sourceMessage 源消息
     * @return {@link String}
     * @throws IOException ioexception
     */
    public static String toJson(Message sourceMessage)
            throws IOException {
        return JsonFormat.printer().print(sourceMessage);
    }

    /**
     * 原型bean
     *
     * @param targetBuilder 目标构建器
     * @param json          json
     * @return {@link Message}
     * @throws IOException ioexception
     */
    public static Message toProtoBean(Message.Builder targetBuilder, String json) throws IOException {
        JsonFormat.parser().merge(json, targetBuilder);
        return targetBuilder.build();
    }
}
