package com.base.http.constants;

import java.nio.charset.StandardCharsets;

public interface HttpConstant {

    int WS_DEFAULT_MAX_FRAME_SIZE = (1 << 15) - 1;
    int WS_PLAY_LOAD_126 = 126;
    int WS_PLAY_LOAD_127 = 127;

    /**
     * Post 最大长度
     */
    int maxPostSize = 2 * 1024 * 1024;

    String SCHEMA_HTTP = "http";
    /**
     * Horizontal space
     */
    byte SP = 32;

    /**
     * Carriage return \n
     */
    byte CR = 13;

    /**
     * Line feed character
     */
    byte LF = 10;

    /**
     * Colon ':'
     */
    byte COLON = 58;


    /**
     * Horizontal space
     */
    char SP_CHAR = (char) SP;

    char COLON_CHAR = COLON;

    byte[] CRLF_BYTES = {HttpConstant.CR, HttpConstant.LF};

    String CRLF = "\r\n";

    byte[] CHUNKED_END_BYTES = "0\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
}
