package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EuConfigs {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile List<Conf> config = Collections.emptyList();

    private static final Logger log = LoggerFactory.getLogger(EuConfigs.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                Set<String> configs = redis.smembers("eu_config");
                ArrayList<Conf> tmpConfigs = new ArrayList<>();
                configs.forEach(conf -> {
                    try {
                        String[] c = conf.split(":");
                        tmpConfigs.add(new Conf(c[0], c[1], c[2], Integer.parseInt(c[3])));
                    } catch (Exception e) {
                        log.error("parse eu_config error with conf:{}", conf);
                    }
                });
                config = tmpConfigs;
            } catch (Exception e) {
                log.error("load eu_config failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static List<Conf> get() {
        return config;
    }

    @AllArgsConstructor
    @Getter
    public static class Conf {
        String key;
        String facet;
        String alias;
        Integer views;
    }
}
