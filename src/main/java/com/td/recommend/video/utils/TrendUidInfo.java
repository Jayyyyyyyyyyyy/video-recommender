package com.td.recommend.video.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * add by sunjian 2021/09/08
 */

@Service
public class TrendUidInfo {
    private static final Logger log = LoggerFactory.getLogger(TrendUidInfo.class);
    private static volatile List<String> trendUidTextList = Collections.emptyList();
    private static final Map<String,Integer> filterTypes = new LinkedHashMap<String,Integer>();
    private static final Map<String,Integer> filterTypes1 = new LinkedHashMap<String,Integer>();
    private static final Map<String,Integer> filterTypes11 = new LinkedHashMap<String,Integer>();

    static {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
            try {
                RedisClientSingleton redis = RedisClientSingleton.general;
                trendUidTextList = redis.lrange("trendUidInfo",0,-1);
                if (trendUidTextList == null || trendUidTextList.size()==0) {
                    log.error("talent uids is empty");
                }
            } catch (Exception e) {
                log.error("talent uids load failed", e);
            }
        }, 0, 1, TimeUnit.MINUTES);

        filterTypes1.put("tfollow", 3);
        filterTypes1.put("vxfollow", 2);
        filterTypes1.put("thot", 1);

        filterTypes11.put("tuidfollow", 1);
    }

    public static List<String> get() {
        return trendUidTextList;
    }

    public static Map<String,Integer> getFilterTypes(int ihf){
        if(ihf==1){
            return filterTypes1;
        }
        else if(ihf==11){
            return filterTypes11;
        }
        else{
            return filterTypes;
        }
    }
}
