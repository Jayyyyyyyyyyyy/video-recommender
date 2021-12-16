package com.td.recommend.video.datasource;

import com.td.recommend.video.utils.RedisClientSingleton;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Mp3CopyrightData {
    private static Logger LOG = LoggerFactory.getLogger(Mp3CopyrightData.class);

    private static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private static String redis_hash_key = "mp3_copyright";
    private static final RedisClientSingleton redis = RedisClientSingleton.recreason;

    @Getter
    private static Set<String> midRiskMp3Set = new HashSet<>();

    @Getter
    private static Set<String> highRiskMp3Set = new HashSet<>();


    static {
        loadData();
        scheduledExecutorService.scheduleAtFixedRate(Mp3CopyrightData::loadData, 0, 10, TimeUnit.MINUTES);
    }



    private static void loadData() {
       Map<String, String> fieldvalues = redis.hgetall(redis_hash_key);
       if (fieldvalues==null || fieldvalues.size()<=0) {
           LOG.error("get mp3_copyright failed.");
           return;
       }
       Set<String> tmpmidRiskMp3Set = new HashSet<>();
       Set<String> tmphighRiskMp3Set = new HashSet<>();
       fieldvalues.forEach(
               (k,v)-> {
                   if (v.equals("中风险")){
                       tmpmidRiskMp3Set.add(k);
                   }else if (v.equals("高风险")) {
                       tmphighRiskMp3Set.add(k);
                   }
               }
       );
        midRiskMp3Set.clear();
        highRiskMp3Set.clear();
        midRiskMp3Set.addAll(tmpmidRiskMp3Set);
        highRiskMp3Set.addAll(tmphighRiskMp3Set);
        LOG.info("reload mp3 copyright succ, midrisk size:{}, highrisk size:{}",midRiskMp3Set.size(),highRiskMp3Set.size());
    }


    public static void main(String[] argv) {

       Set<String> highset =  Mp3CopyrightData.getHighRiskMp3Set();
       Set<String> midset = Mp3CopyrightData.getMidRiskMp3Set();

       System.out.println("highset:"+String.join(",",highset));
       System.out.println("midset:"+String.join(",", midset));

    }



}
