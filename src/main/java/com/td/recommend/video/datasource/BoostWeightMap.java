package com.td.recommend.video.datasource;

import com.td.recommend.ss.TrafficEqualization;
import com.td.recommend.ss.TrafficEqualizationHolder;
import com.td.recommend.video.utils.RedisClientSingleton;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BoostWeightMap {
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    @Getter
    private static volatile Map<String, Double> talentUidMap = Collections.emptyMap();
    @Getter
    private static volatile Map<String, Double> talentVidMap = Collections.emptyMap();
    @Getter
    private static volatile Map<String, Double> horseVidMap = Collections.emptyMap();
    @Getter
    private static volatile Set<String> horseUidSet = Collections.emptySet();
    @Getter
    private static volatile Map<String, Double> talentUids_lt3 = Collections.emptyMap();

    private static final TrafficEqualization trafficEqualization = TrafficEqualizationHolder.getInstance();
    private static final Logger log = LoggerFactory.getLogger(BoostWeightMap.class);

    static {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            loadHorseVids();
            loadHorseUids();
            loadTalentUids();
            loadTalentVids();
            loadTalentUids_lt3();
        }, 0, 1, TimeUnit.MINUTES);
    }

    private static void loadHorseVids() {
        try {
            RedisClientSingleton redis = RedisClientSingleton.boost;
            Set<String> allVids = redis.smembers("boosted_vids");
            HashMap<String, Double> map = new HashMap<>();
            for (String vid : allVids) {
                String weight = redis.get("boosted_vid_" + vid);
                map.put(vid, Double.parseDouble(weight));
            }
            horseVidMap = map;
        } catch (Exception e) {
            log.error("loadHorseVids failed", e);
        }
    }

    private static void loadHorseUids() {
        try {
            RedisClientSingleton redis = RedisClientSingleton.boost;
            horseUidSet = redis.smembers("boosted_uids");
        } catch (Exception e) {
            log.error("loadHorseUids failed", e);
        }
    }

    private static void loadTalentUids() {
        try {
            Set<String> allUserIds = trafficEqualization.getAllUserIds();
            RedisClientSingleton redis = RedisClientSingleton.boost;
            HashMap<String, Double> map = new HashMap<>();
            for (String uid : allUserIds) {
                String value = redis.get("user_exposure_weight_" + uid);
                Double weight = extractWeight(value);
                map.put(uid, weight);
            }
            talentUidMap = map;
        } catch (Exception e) {
            log.error("loadWeightedUids failed", e);
        }
    }

    private static void loadTalentUids_lt3() {
        try {
            RedisClientSingleton redis = RedisClientSingleton.boost;
            Map<String, String> talentUids_string = redis.hgetall("talentUids_lt3");
            if(talentUids_string==null || talentUids_string.size()<=0){
                log.error("talent Uids_lt3 is empty");
            }
            else{
                HashMap<String, Double> map = new HashMap<>();
                talentUids_string.forEach((uid, value) ->{
                    map.put(uid, extractWeight_double(value));
                });
                talentUids_lt3 = map;
            }

        } catch (Exception e) {
            log.error("loadWeightedUids_lt3 failed", e);
        }
    }

    private static void loadTalentVids() {
        try {
            RedisClientSingleton redis = RedisClientSingleton.boost;
            Set<String> videos = redis.smembers("video_exposure_weight");
            talentVidMap = videos.stream().collect(Collectors.toMap(i -> i.split(":")[0], i -> Double.parseDouble(i.split(":")[1])));
        } catch (Exception e) {
            log.error("loadTalentVids failed", e);
        }
    }

    private static Double extractWeight(String value) {
        double weight;
        try {
            weight = Double.parseDouble(value.split(",")[1]);
        } catch (Exception e) {
            weight = 1.0;
            log.info("extract weight failed: {}", value, e);
        }
        return weight;
    }

    private static Double extractWeight_double(String value) {
        double weight;
        try {
            weight = Double.parseDouble(value);
        } catch (Exception e) {
            weight = 1.0;
            log.info("extract weight failed: {}", value, e);
        }
        return weight;
    }

    public static void main(String[] args) {
        System.out.println("lj" + BoostWeightMap.getTalentUidMap());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("lj" + BoostWeightMap.getTalentUidMap());
        System.out.println("lj" + BoostWeightMap.getHorseVidMap());

    }
}
