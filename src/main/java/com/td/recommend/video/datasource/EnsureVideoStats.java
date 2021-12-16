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

public class EnsureVideoStats {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile List<String> firstCats = Collections.emptyList();
    private static volatile List<String> smallFirstCats = Collections.emptyList();
    private static volatile List<String> virtualFirstCats = Collections.emptyList();


    private static final Logger log = LoggerFactory.getLogger(EnsureVideoStats.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                String cats = redis.get("ensure_cats");
                String small_cats = redis.get("small_ensure_cats");
                String virtual_cats = redis.get("virtual_ensure_cats");
                if (StringUtils.isNotBlank(cats)) {
                    firstCats = Arrays.asList(cats.split(","));
                }
                if (StringUtils.isNotBlank(small_cats)) {
                    smallFirstCats = Arrays.asList(small_cats.split(","));
                }
                if (StringUtils.isNotBlank(virtual_cats)) {
                    virtualFirstCats = Arrays.asList(virtual_cats.split(","));
                }
                log.info("load ensure_cats={}, small_ensure_cats={}, virtual_ensure_cats={}", cats, small_cats, virtual_cats);

            } catch (Exception e) {
                log.error("load ensure_cats or small_ensure_cats or virtual_ensure_cats failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static List<String> getFirstCats() {
        return firstCats;
    }

    public static List<String> getSmallFirstCats() {
        return smallFirstCats;
    }

    public static List<String> getVirtualFirstCats() {
        return virtualFirstCats;
    }

}
