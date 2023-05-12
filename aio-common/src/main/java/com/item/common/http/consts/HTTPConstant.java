
package com.item.common.http.consts;

import java.nio.charset.StandardCharsets;

public interface HTTPConstant {

    int WS_DEFAULT_MAX_FRAME_SIZE = (1 << 15) - 1;
    int WS_PLAY_LOAD_126 = 126;
    int WS_PLAY_LOAD_127 = 127;

    /**
     * Post 最大长度
     */
    int maxPostSize = 2 * 1024 * 1024;

    String SCHEMA_HTTP = "http";
    /**
     * 空格
     */
    byte SP = 32;

    /**
     *  \n
     */
    byte CR = 13;

    /**
     * \r
     */
    byte LF = 10;

    /**
     * Colon ':'
     */
    byte COLON = 58;

    byte[] CRLF_BYTES = {HTTPConstant.CR, HTTPConstant.LF};

    String CRLF = "\r\n";

    byte[] CHUNKED_END_BYTES = "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);

}