package com.base.io.reactor;

import java.io.IOException;

/**
 *  主从分离io
 * 反应器应用
 *
 * @author bai
 * @date 2023/06/15
 */
public class ReactorApplication {


    public static void main(String[] args) throws IOException {
        new  ReactorSocketServer("127.0.0.1",7777).start();
    }
}
