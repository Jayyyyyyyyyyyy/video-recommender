package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShowDanceConfigs {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile Set<String> album = Collections.emptySet();
    private static final Logger log = LoggerFactory.getLogger(ShowDanceConfigs.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                album = redis.smembers("active:album");
            } catch (Exception e) {
                log.error("load in_config failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static Set<String> getAlbum() {
        return album;
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10000);
        System.out.println(ShowDanceConfigs.getAlbum());
    }
}
