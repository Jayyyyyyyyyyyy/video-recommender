package com.td.recommend.video.datasource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.td.recommend.video.utils.RedisClientSingleton;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RecReasonsMap {
    private static Logger LOG = LoggerFactory.getLogger(RecReasonsMap.class);
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final static String recReasonKey = "rec_reason";

    @Getter
    public static  Map<String, String> vidToReasonMap = new HashMap<>();
    private static final RedisClientSingleton redis = RedisClientSingleton.recreason;

    static {
        loadData();
        scheduledExecutorService.scheduleAtFixedRate(RecReasonsMap::loadData, 0, 10, TimeUnit.MINUTES);
    }

    private static void loadData() {
        Map<String, String> hgetMap = redis.hgetall(recReasonKey);
        if (hgetMap != null) {
            Map<String, String> vidToReasonMapTmp = new HashMap<>();
            for (String key : hgetMap.keySet()) {
                String reason = extractReason(hgetMap.get(key));
                if (reason != null) {
                    vidToReasonMapTmp.put(key, reason);
                }

            }
            if (vidToReasonMapTmp.size()>0) {
                vidToReasonMap.clear();
                vidToReasonMap.putAll(vidToReasonMapTmp);
                LOG.info("reload reason data, size:{}",vidToReasonMap.size());
            }
        }
    }

    private static String extractReason(String value) {
        JSONObject obj = JSON.parseObject(value);
        return obj.getString("name");
    }

    public static Optional<String> getReasonById(String id) {
        if (vidToReasonMap.containsKey(id)) {
            return Optional.ofNullable(vidToReasonMap.get(id));
        }
        return Optional.empty();
    }


    public static void main(String[] argv) {
        Map<String, String> idlist = RecReasonsMap.getVidToReasonMap();
        System.out.println(idlist.size());
//
        for (String id: idlist.keySet()) {
            System.out.println("id:"+id+" value:"+idlist.get(id));
        }

        String str = "1889 1890 1891 1892 1897 1898 1899 1990 1905 1906 1907 1908 1913 1914 1915 1916 1917 1918 1919 1920 2089 2090 2091 2092 2093 2094";
        String[] idarray = str.split(" ");

       System.out.println(String.join("\",\"",idarray));

//        List<String> idlist =  Arrays.asList("1","2","3","4");
//        List<String> sublist = idlist.subList(0,1);
//        System.out.println(String.join(",", sublist));
    }
}
