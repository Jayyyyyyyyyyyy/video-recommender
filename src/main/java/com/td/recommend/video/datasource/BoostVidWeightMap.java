package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;

import com.twitter.jvm.Opt;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BoostVidWeightMap {
    private static final Logger LOG = LoggerFactory.getLogger(BoostVidWeightMap.class);
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static volatile Map<String, WeightValue> weightMap = new HashMap<>();

    public static final String WhiteVidWeightKey = "whitevidweightkey";

    private static final RedisClientSingleton redis = RedisClientSingleton.recreason;


    static {
        loadData();
        scheduledExecutorService.scheduleAtFixedRate(BoostVidWeightMap::loadData, 0, 10, TimeUnit.MINUTES);
    }


    private static Optional<WeightValue> getVidWeight(String value) {
        try {
            String[] splits = value.split("\t");
            if (splits.length < 14) {
                return Optional.empty();
            }

            long expireDay = Long.valueOf(splits[11]);


            long curtime = System.currentTimeMillis();
            long starttime = Long.valueOf(splits[13]);
            long diffday = getDiffDay(curtime, starttime);
            if (diffday > expireDay) {
                return Optional.empty();
            }
            double weight = Double.valueOf(splits[10]);
            int inviewnum = Integer.valueOf(splits[12]);

            return Optional.ofNullable(new WeightValue(weight, inviewnum));
        }catch (Exception ex) {
            return Optional.empty();
        }

    }

    private static long getDiffDay(long curtime, long starttime) {
        return (curtime - starttime)/(1000*86400);
    }

    public static class WeightValue {
        public WeightValue(double weight, int inviewNumThreshold) {
            this.weight = weight;
            this.inviewNumThreshold = inviewNumThreshold;
        }

        @Getter
        @Setter
        public double weight;

        @Getter
        @Setter
        public int inviewNumThreshold;

    }
    private static void loadData() {
        Map<String, String> hgetMap = redis.hgetall(WhiteVidWeightKey);
        if (hgetMap != null) {
            Map<String, WeightValue> vidToReasonMapTmp = new HashMap<>();
            for (String key : hgetMap.keySet()) {
                Optional<WeightValue> weightValueOptional = getVidWeight(hgetMap.get(key));
                if (weightValueOptional.isPresent()) {
                    vidToReasonMapTmp.put(key,weightValueOptional.get());
                }

            }
            if (vidToReasonMapTmp.size()>0) {
                weightMap.clear();
                weightMap.putAll(vidToReasonMapTmp);
                LOG.info("reload reason data, size:{}",vidToReasonMapTmp.size());
            }
        }
    }

    public static WeightValue getWeightValue(String vid) {
        return weightMap.get(vid);
    }

    public static double getWeight(String vid, double inviewNum) {
        WeightValue weightValue = weightMap.get(vid);
        if (weightValue == null || inviewNum > weightValue.inviewNumThreshold) {
            return 1.0;
        }
        return weightValue.weight;

    }

    public static void main(String[] argv) {

        System.out.println("size:"+weightMap.size());
        for (String key : weightMap.keySet()) {
            WeightValue weightValue = weightMap.get(key);
            System.out.println(key+" weight:"+weightValue.weight+" inview:"+weightValue.inviewNumThreshold);
        }
//        RedisClientSingleton redis = RedisClientSingleton.recreason;
//        String path = "/Users/pansm/JCode/java-demo/test1.txt";
//        ArrayList<String> arrayList = new ArrayList<>();
//        String curtime = String.valueOf(System.currentTimeMillis());
//        try {
//            FileReader fr = new FileReader(path);
//            BufferedReader bf = new BufferedReader(fr);
//            String str;
//            // 按行读取字符串
//            while ((str = bf.readLine()) != null) {
//                str = str+"\t"+curtime;
//                String[] splits = str.split("\t");
//                System.out.println(splits[1]+" "+splits[10]+" "+splits[11]+" "+splits[12]+" "+splits[13]);
//                redis.hset(WhiteVidWeightKey, splits[1], str);
//            }
//            bf.close();
//            fr.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

}
