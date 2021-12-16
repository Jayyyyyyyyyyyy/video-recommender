package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TeachingResearchVidSet {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile Set<String> vidSet = Collections.emptySet();


    private static final Logger log = LoggerFactory.getLogger(TeachingResearchVidSet.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                List<String> teaches = redis.lrange("feed_test_video:groups:list", 0, -1);
                Set<String> titles = redis.smembers("titleresearch:vid");
                HashSet<String> tmpVidSet = new HashSet<>();
                tmpVidSet.addAll(titles);
                tmpVidSet.addAll(teaches);
                vidSet = tmpVidSet;
            } catch (Exception e) {
                log.error("load TeachingResearchVidSet failed", e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public static Set<String> get() {
        return vidSet;
    }
}
