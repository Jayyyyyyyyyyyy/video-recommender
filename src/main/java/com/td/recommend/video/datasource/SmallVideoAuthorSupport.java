package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SmallVideoAuthorSupport {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile List<String> uidList = Collections.emptyList();


    private static final Logger log = LoggerFactory.getLogger(SmallVideoAuthorSupport.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                String uids = redis.get("svauthorsupport");
                if (uids != null) {
                    uidList = Arrays.asList(uids.split(","));
                }
                log.info("load svauthorsupport size={}", uidList.size());
            } catch (Exception e) {
                log.error("load svauthorsupport failed", e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public static List<String> getUidList() {
        return uidList;
    }
}
