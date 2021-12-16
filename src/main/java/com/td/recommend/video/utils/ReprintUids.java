package com.td.recommend.video.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ReprintUids {
    private static final Logger log = LoggerFactory.getLogger(ReprintUids.class);
    private static volatile Set<String> uids = Collections.emptySet();

    static {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                uids = redis.smembers("reprint_uids");
            } catch (Exception e) {
                log.error("reprint_uids load failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static Set<String> get() {
        return uids;
    }
}
