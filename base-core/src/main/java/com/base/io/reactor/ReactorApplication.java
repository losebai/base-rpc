package com.base.io.reactor;

import java.io.IOException;

public class ReactorApplication {


    public static void main(String[] args) throws IOException {
        new  ReactorSocketServer("127.0.0.1",7777).start();
    }
}
