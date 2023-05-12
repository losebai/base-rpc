package com.base.http.processor;

import cn.hutool.core.util.ByteUtil;
import com.base.core.util.ByteStringUtil;
import com.base.http.enums.DecodePartEnum;
import com.base.http.enums.HttpStatus;
import com.base.http.exception.HttpException;
import com.base.http.module.HttpRequest;
import com.base.core.processor.Processor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.smartboot.socket.transport.AioSession;
import org.smartboot.socket.transport.WriteBuffer;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class HttpRequestProcessor implements Processor<HttpRequest> {

    private static final int MAX_LENGTH = 255 * 1024;

    @Override
    public void process(AioSession session, HttpRequest request) {
        WriteBuffer writeBuffer = session.writeBuffer();
        try {
            byte[] bytes = ByteStringUtil.ToByte(request);
            writeBuffer.writeInt(bytes.length);
            writeBuffer.write(bytes);
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    private void doHttpHeader(HttpRequest request) throws IOException {
        methodCheck(request);
        uriCheck(request);
        request.setDecodePartEnum(DecodePartEnum.BODY);
    }


    /**
     * RFC2616 5.1.1 方法标记指明了在被 Request-URI 指定的资源上执行的方法。
     * 这种方法是大小写敏感的。 资源所允许的方法由 Allow 头域指定(14.7 节)。
     * 响应的返回码总是通知客户某个方法对当前资源是否是被允许的，因为被允许的方法能被动态的改变。
     * 如果服务器能理解某方法但此方法对请求资源不被允许的，
     * 那么源服务器应该返回 405 状态码(方法不允许);
     * 如果源服务器不能识别或没有实现某个方法，那么服务器应返回 501 状态码(没有实现)。
     * 方法 GET 和 HEAD 必须被所有一般的服务器支持。 所有其它的方法是可选的;
     * 然而，如果上面的方法都被实现， 这些方法遵循的语意必须和第 9 章指定的相同
     */
    private void methodCheck(HttpRequest request) {
        if (request.getMethod() == null) {
            throw new HttpException(HttpStatus.NOT_IMPLEMENTED);
        }
    }

    /**
     * RFC2616 3.2.1
     * HTTP 协议不对 URI 的长度作事先的限制，服务器必须能够处理任何他们提供资源的 URI，并 且应该能够处理无限长度的 URIs，这种无效长度的 URL 可能会在客户端以基于 GET 方式的 请求时产生。如果服务器不能处理太长的 URI 的时候，服务器应该返回 414 状态码(此状态码 代表 Request-URI 太长)。
     * 注:服务器在依赖大于 255 字节的 URI 时应谨慎，因为一些旧的客户或代理实现可能不支持这 些长度。
     */
    private void uriCheck(HttpRequest request) {
        String originalUri = request.getRequestUri();
        if (StringUtils.length(originalUri) > MAX_LENGTH) {
            throw new HttpException(HttpStatus.URI_TOO_LONG);
        }
        /**
         *http_URL = "http:" "//" host [ ":" port ] [ abs_path [ "?" query ]]
         *1. 如果 Request-URI 是绝对地址(absoluteURI)，那么主机(host)是 Request-URI 的 一部分。任何出现在请求里 Host 头域的值应当被忽略。
         *2. 假如 Request-URI 不是绝对地址(absoluteURI)，并且请求包括一个 Host 头域，则主 机(host)由该 Host 头域的值决定.
         *3. 假如由规则1或规则2定义的主机(host)对服务器来说是一个无效的主机(host)， 则应当以一个 400(坏请求)错误消息返回。
         */
        if (originalUri.charAt(0) == '/') {
            request.setRequestUri(originalUri);
            return;
        }
        int schemeIndex = originalUri.indexOf("://");
        if (schemeIndex > 0) {//绝对路径
            int uriIndex = originalUri.indexOf('/', schemeIndex + 3);
            if (uriIndex == StringUtils.INDEX_NOT_FOUND) {
                request.setRequestUri("/");
            } else {
                request.setRequestUri(StringUtils.substring(originalUri, uriIndex));
            }
            request.setScheme(StringUtils.substring(originalUri, 0, schemeIndex));
        } else {
            request.setRequestUri(originalUri);
        }
    }
}
