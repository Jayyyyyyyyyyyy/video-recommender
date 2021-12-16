package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExploreUserCatsMap {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile List<String> virtualSubCats = Collections.emptyList();
    private static volatile List<String> subCats = Collections.emptyList();


    private static final Logger log = LoggerFactory.getLogger(ExploreUserCatsMap.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                String eu_subcats = redis.get("eu_subcats");
                String virtual_eu_subcats = redis.get("virtual_eu_subcats");
                if (StringUtils.isNotBlank(eu_subcats)) {
                    subCats = Arrays.asList(eu_subcats.split(","));
                }
                if (StringUtils.isNotBlank(virtual_eu_subcats)) {
                    virtualSubCats = Arrays.asList(virtual_eu_subcats.split(","));
                }
            } catch (Exception e) {
                log.error("load eu_subcats virtual_eu_subcats failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static List<String> getSubcats() {
        return subCats;
    }

    public static List<String> getVirtualSubcats() {
        return virtualSubCats;
    }
}
