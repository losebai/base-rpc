package com.base.http.module;

import lombok.Data;

@Data
public class HeaderValue {
    /**
     * name
     */
    private String name;
    /**
     * Value 值
     */
    private String value;
    /**
     * 同名Value
     */
    private HeaderValue nextValue;

    public HeaderValue(String name, String value) {
        this.name = name;
        this.value = value;
    }
}
