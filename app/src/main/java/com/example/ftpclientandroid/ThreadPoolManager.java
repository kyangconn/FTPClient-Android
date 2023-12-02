package com.example.ftpclientandroid;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private static ThreadPoolExecutor threadPoolExecutor;

    public static synchronized ThreadPoolExecutor getInstance() {
        if (threadPoolExecutor == null) {
            threadPoolExecutor = new ThreadPoolExecutor(
                    2, 4, 60, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(120),
                    Executors.defaultThreadFactory(),
                    new ThreadPoolExecutor.AbortPolicy()
            );
        }
        return threadPoolExecutor;
    }
}
