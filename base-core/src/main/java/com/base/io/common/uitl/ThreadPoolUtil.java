package com.base.io.common.uitl;

import java.util.concurrent.*;

/**
 * 线程池跑龙套
 *
 * @author bai
 * @date 2023/06/13
 */
public class ThreadPoolUtil {

    private static final class ExecutorHolder {
        static final ExecutorService executor = new ThreadPoolExecutor(
                10,
                20,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    /**
     * 获取线程池
     *
     * @return 线程池
     */
    public static ExecutorService getThreadPool() {
        // 根据实际情况调整参数
        return ExecutorHolder.executor;
    }


    /**
     * 新线程池
     *
     * @return {@link ExecutorService}
     */
    public static ExecutorService newThreadPool(){

        return new ThreadPoolExecutor(
                10,
                20,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );
    }

    /**
     * 提交
     *
     * @param runnable 可运行
     * @return {@link Future}<{@link ?}>
     */
    public static Future<?> submit(Runnable runnable){
        if (ExecutorHolder.executor == null){
            throw new RuntimeException("ThreadPool is null");
        }
        return ExecutorHolder.executor.submit(runnable);

    }
}
