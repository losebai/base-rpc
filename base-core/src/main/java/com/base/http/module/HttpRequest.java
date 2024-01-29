package com.base.http.module;

import cn.hutool.core.util.StrUtil;
import com.base.core.util.HttpUtils;
import com.base.http.enums.HeaderNameEnum;
import com.base.http.constants.HttpConstant;
import com.base.http.enums.HttpTypeEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public final class HttpRequest {

    /**
     * 协议
     */
    private String scheme = HttpConstant.SCHEMA_HTTP;

    /**
     * Http协议版本
     */
    private String protocol;

    String method;

    String requestUri;

    String requestUrl;

    private String remoteAddr;

    private String remoteHost;

    private String hostHeader;

    /**
     * Http请求头
     */
    private final List<HeaderValue> headers = new ArrayList<>(8);

    /**
     * 请求参数
     */
    private Map<String, String[]> parameters;

    /**
     * 跟在URL后面的请求信息
     */
    private String queryString;

    /**
     * Post表单
     */
    private String formUrlencoded;

    private int headerSize = 0;

    private HttpTypeEnum type = null;


    public void setHeader(String headerName, String value) {
        if (headerSize < headers.size()) {
            HeaderValue headerValue = headers.get(headerSize);
            headerValue.setName(headerName);
            headerValue.setValue(value);
        } else {
            headers.add(new HeaderValue(headerName, value));
        }
        headerSize++;
    }

    public HttpTypeEnum getRequestType() {
        return type;
    }

    public String getHeader(String headName) {
        for (int i = 0; i < headerSize; i++) {
            HeaderValue headerValue = headers.get(i);
            if (headerValue.getName().equalsIgnoreCase(headName)) {
                return headerValue.getValue();
            }
        }
        return null;
    }


    public String getRequestURL() {
        if (requestUrl != null) {
            return requestUrl;
        }
        if (requestUri.startsWith("/")) {
            requestUrl = getScheme() + "://" + getHeader(HeaderNameEnum.HOST.getName()) + requestUri;
        } else {
            requestUrl = requestUri;
        }
        return requestUrl;
    }

    public String[] getParameterValues(String name) {
        if (parameters != null) {
            return parameters.get(name);
        }
        parameters = new HashMap<>();
        //识别url中的参数
        String urlParamStr = queryString;
        if (StrUtil.isNotEmpty(urlParamStr)) {
            urlParamStr = StrUtil.subBefore(urlParamStr, "#",false);
            HttpUtils.decodeParamString(urlParamStr, parameters);
        }

        if (formUrlencoded != null) {
            HttpUtils.decodeParamString(formUrlencoded, parameters);
        }
        return getParameterValues(name);
    }

}
