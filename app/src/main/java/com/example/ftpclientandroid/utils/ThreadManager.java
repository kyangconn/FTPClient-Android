package com.example.ftpclientandroid.utils;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author kyang
 */
public class ThreadManager {
    private static ThreadPoolExecutor threadPoolExecutor;

    public static synchronized ThreadPoolExecutor getInstance() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(
                    2,
                    16,
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    new ThreadPoolExecutor.DiscardOldestPolicy()
            );
        }
        return threadPoolExecutor;
    }
}
