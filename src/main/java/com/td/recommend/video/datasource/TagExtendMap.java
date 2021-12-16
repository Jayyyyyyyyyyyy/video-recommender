package com.td.recommend.video.datasource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.td.recommend.video.utils.RedisClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TagExtendMap {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static Cache<String, List<String>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .build();

    private static final Logger log = LoggerFactory.getLogger(TagExtendMap.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            loadTagExtendKeys("vmp3_*");
            loadTagExtendKeys("vauthor_uid_*");
        }, 0, 1, TimeUnit.HOURS);
    }

    private static void loadTagExtendKeys(String pattern) {
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            Set<String> keys = redis.keys(pattern);
            keys.forEach(key -> cache.put(key, redis.lrange(key, 0, 10)));
            log.info("load " + pattern + " extend keys succeed with size={}", keys.size());
        } catch (Exception e) {
            log.error("load tag extend keys failed", e);
        }
    }

    public static List<String> get(String key) {
        List<String> result = cache.getIfPresent(key);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
}
