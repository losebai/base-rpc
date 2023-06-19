package com.base.io.common;

/**
 * 协议常量
 *
 * @author bai
 * @date 2023/06/19
 */
public interface ProtocolConst {


    byte[] START = "init".getBytes();



    byte[] READ = new byte[1];



    byte[] CLOSE = "close".getBytes();
}
