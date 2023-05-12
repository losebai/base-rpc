package com.base.http.module;

import lombok.Data;


@Data
public class HttpResponse  {

    String version;

    Integer code;

    String message;

    String date;

    String server;

    String lastModified;

    String eTag;

    String acceptRanges;

    Integer contentLength;

    String contentType;

    Object data;

}
