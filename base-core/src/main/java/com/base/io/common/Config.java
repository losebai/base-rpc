package com.base.io.common;

public interface Config {

    int BUFFER_SIZE = 1024;

    int READ_BUFFER_SIZE = 512;

    int WRITE_BUFFER_SIZE = 512;

    int SubReactors_SIZE = 1; // Runtime.getRuntime().availableProcessors() >> 1; // 从的数量
}
