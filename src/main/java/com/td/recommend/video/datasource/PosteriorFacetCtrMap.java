package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PosteriorFacetCtrMap {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static final Map<String, Map<String, Double>> facetCtrMap = new ConcurrentHashMap<>();
    private static final List<String> facets = Arrays.asList("vcat,vsubcat,vtag,vauthor_uid,vmp3,vexercise_body".split(","));
    private static final Logger log = LoggerFactory.getLogger(PosteriorFacetCtrMap.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                for (String facet : facets) {
                    String redisKey = facet + "_ctr";
                    RedisClientSingleton redis = RedisClientSingleton.general;
                    List<String> facetIdCtrs = redis.lrange(redisKey, 0, -1);
                    Map<String, Double> facetIdCtrMap = facetIdCtrs.stream().collect(Collectors.toMap(i -> i.split(":")[0], i -> Double.valueOf(i.split(":")[1]), (v1, v2) -> v1));
                    facetCtrMap.put(facet, facetIdCtrMap);
                }
            } catch (Exception e) {
                log.error("load PosteriorFacetCtrMap failed", e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public static Map<String, Map<String, Double>> get() {
        return facetCtrMap;
    }

    public static Map<String, Double> get(String facet) {
        Map<String, Double> ctrMap = facetCtrMap.get(facet);
        if (ctrMap == null) {
            return Collections.emptyMap();
        }
        return ctrMap;
    }

    public static void main(String[] args) {

    }
}
