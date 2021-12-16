package com.td.recommend.video.datasource;

import com.td.data.profile.utils.DateUtils;
import com.td.recommend.video.utils.RedisClientSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveUserInfo {
    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static Map<String,Double> liveUserWeightMap = new HashMap<>(10);
    private static volatile Set<String> livingUserSet = new HashSet<>(10);
    private static volatile List<String> liveUserList = new ArrayList<>(10);
    private final static String prefix = "recommend_liver:";
    private static final Logger log = LoggerFactory.getLogger(LiveUserInfo.class);

    static {
        dataInit();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"));
        ZonedDateTime nextRunDay = now.withHour(0).withMinute(0).withSecond(0);
        if(now.compareTo(nextRunDay) > 0){
            nextRunDay = nextRunDay.plusDays(1);
        }
        Duration durationDay = Duration.between(now, nextRunDay);
        long initalDelay_day = durationDay.getSeconds();
        //每天0点执行一次
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                dataInit();
                log.info("every day 00:00:00 load liveUserWeightMap={}, liveUserList={}", liveUserWeightMap.size(), liveUserList.size());
            } catch (Exception e) {
                log.error("load liveUser_day info failed", e);
            }
        }, initalDelay_day, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);

        ZonedDateTime nextRunMinute = now.withSecond(1);
        if(now.compareTo(nextRunMinute) > 0){
            nextRunMinute = nextRunMinute.plusMinutes(1);
        }
        Duration durationMinute = Duration.between(now, nextRunMinute);
        long initalDelay_minute = durationMinute.getSeconds();
        //每分钟执行一次，整分执行
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                calLivingUidSet();
                log.info("every minus load liveUserWeightMap={}, liveUserList={}, livingUserSet={}", liveUserWeightMap.size(), liveUserList.size(), livingUserSet.size());
            } catch (Exception e) {
                log.error("load livingUser_minute info failed", e);
            }
        }, initalDelay_minute, TimeUnit.MINUTES.toSeconds(1), TimeUnit.SECONDS);

    }

    public static void dataInit(){
        liveUserList.clear();
        livingUserSet.clear();
        liveUserWeightMap.clear();
        String liveDay = DateUtils.toDateTime(System.currentTimeMillis(),"yyyyMMdd");
        RedisClientSingleton redis = RedisClientSingleton.general;
        List<String> list = redis.lrange(prefix + liveDay,0,-1);
        Long now = System.currentTimeMillis()/1000;
        list.stream().forEach(
            uid_start_end_weight->{
                String[] arr = uid_start_end_weight.split("_",4);
                String uid = arr[0];
                long startTime = Long.valueOf(arr[1]);
                long endTime = Long.valueOf(arr[2]);
                Double userWeight = Double.valueOf(arr[3]);
                liveUserList.add(uid_start_end_weight);
                liveUserWeightMap.put(uid,userWeight);
                if(now >= startTime && now < endTime){
                    livingUserSet.add(arr[0]);
                }
            }
        );
    }
    private static void calLivingUidSet(){
        Long now = System.currentTimeMillis()/1000;
        //同一用户一天内有多次直播，livingUserSet中的uid可能会被误删
        Set<String> multiInfoSameUserSet = new HashSet<>(5);
        liveUserList.stream().forEach(
                uid_start_end_weight->{
                    String[] arr = uid_start_end_weight.split("_",4);
                    String uid = arr[0];
                    Long startTime = Long.valueOf(arr[1]);
                    Long endTime = Long.valueOf(arr[2]);
                    if(now >= startTime && now < endTime){
                        livingUserSet.add(uid);
                        multiInfoSameUserSet.add(uid);
                    }else {
                        if(livingUserSet.contains(uid) && !multiInfoSameUserSet.contains(uid)){
                            livingUserSet.remove(uid);
                        }
                    }
                }
        );
    }
    public static Set<String> getLivingUidSet(){return livingUserSet;}
    public static Double getUserWeight(String uid) {
        if(liveUserWeightMap.containsKey(uid)){
           return  liveUserWeightMap.get(uid);
        }
        return 1.0;
    }
}
