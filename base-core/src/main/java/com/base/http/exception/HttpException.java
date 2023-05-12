
package com.base.http.exception;


import com.base.http.enums.HttpStatus;
import lombok.Data;


@Data
public class HttpException extends RuntimeException {
    private int httpCode;

    private String desc;

    public HttpException(HttpStatus httpStatus) {
        super(httpStatus.getReasonPhrase());
        this.httpCode = httpStatus.value();
        this.desc = httpStatus.getReasonPhrase();
    }

}
