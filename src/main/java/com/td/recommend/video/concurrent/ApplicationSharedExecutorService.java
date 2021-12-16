package com.td.recommend.video.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import com.td.recommend.video.utils.UserVideoConfig;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2017/6/10.
 */
public class ApplicationSharedExecutorService {
    private static volatile ApplicationSharedExecutorService instance = null;
    private ExecutorService executorService;

    public static final String THREAD_POOL_MAX_SIZE_KEY = "app-threadpool-maxsize";
    public static final String THREAD_POOL_QUEUE_SIZE_KEY = "app-threadpool-queuesize";

    public static ApplicationSharedExecutorService getInstance() {
        if (instance == null) {
            synchronized (ApplicationSharedExecutorService.class) {
                if (instance == null) {
                    instance = new ApplicationSharedExecutorService();
                }
            }
        }
        return instance;
    }

    private ApplicationSharedExecutorService() {
        Config appConfig = UserVideoConfig.getInstance().getAppConfig();

        int maxPoolSize = 500;
        if (appConfig.hasPath(THREAD_POOL_MAX_SIZE_KEY)) {
            maxPoolSize = appConfig.getInt(THREAD_POOL_MAX_SIZE_KEY);
        }

        int queueSize = 300;
        if (appConfig.hasPath(THREAD_POOL_QUEUE_SIZE_KEY)) {
            queueSize = appConfig.getInt(THREAD_POOL_QUEUE_SIZE_KEY);
        }

        executorService = new ThreadPoolExecutor(maxPoolSize, maxPoolSize,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize),
                new ThreadFactoryBuilder().setNameFormat("uservideo-shared-%d").build());
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
