package com.td.recommend.video.datasource;

import com.alibaba.fastjson.JSON;
import com.td.recommend.video.utils.RedisClientSingleton;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EvConfigs {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile Map<String, Conf> config = Collections.emptyMap();
    private static final Logger log = LoggerFactory.getLogger(EvConfigs.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                Set<String> configs = redis.smembers("ev_config");
                Map<String, Conf> tmpConfig = new HashMap<>();
                configs.forEach(conf -> {
                    try {
                        Conf c = JSON.parseObject(conf, Conf.class);
                        tmpConfig.put(c.getKey(), c);
                    } catch (Exception e) {
                        log.error("parse ev_config error with conf:{}", conf);
                    }
                });
                config = tmpConfig;
            } catch (Exception e) {
                log.error("load ev_config failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static Map<String, Conf> get() {
        return config;
    }

    @Getter
    @ToString
    @AllArgsConstructor
    public static class Conf {
        String key;
        double ctr1;
        double ctr2;
        double weight;
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10000);
        System.out.println(EvConfigs.get());
    }
}
