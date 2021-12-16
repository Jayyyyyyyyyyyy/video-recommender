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

public class HomeworkVids {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile Set<String> vidSet = Collections.emptySet();


    private static final Logger log = LoggerFactory.getLogger(HomeworkVids.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                List<String> vids = redis.lrange("homework_vids", 0, -1);
                vidSet = new HashSet<>(vids);
            } catch (Exception e) {
                log.error("load homework_vids failed", e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public static Set<String> get() {
        return vidSet;
    }
    public static boolean contains(String o) {
        return vidSet.contains(o);
    }

    public static void main(String[] args) {

    }
}
