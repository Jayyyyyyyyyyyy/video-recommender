package com.td.recommend.video.utils;

import jdk.nashorn.internal.objects.annotations.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TalentUids {
    private static final Logger log = LoggerFactory.getLogger(TalentUids.class);
    private static volatile List<String> talentUids = Collections.emptyList();
    private static volatile Map<String, String> talent2Cluster = Collections.emptyMap();
    private static volatile Map<String, List<String>> cluster2talent = Collections.emptyMap();
    private static volatile Map<String, String> diu2Cluster = Collections.emptyMap();
    private static volatile Map<String, List<String>> diu2clusterlist = Collections.emptyMap();

    private static volatile Map<String, String> talent2Cluster_v2 = Collections.emptyMap();
    private static volatile Map<String, List<String>> cluster2talent_v2 = Collections.emptyMap();

    private static volatile Map<String, String> talentCF = Collections.emptyMap();
    private static volatile Map<String, List<String>> talentCFlist = Collections.emptyMap();

    private static double clusterUidWeight = 1.0;
    private static double retrieveKeyweight = 1.0;

    static {
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {

            loadTalentUids();
            loadTalent2Cluster();
            //loadDiu2Cluster();
            loadTalent2Clusterv2();
            loadTalentCF();
            loadTalentClusterWeight();

        }, 0, 1, TimeUnit.MINUTES);
    }

    private static void loadTalentUids(){
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            talentUids = redis.lrange("talent_uids",0,-1);
            if (talentUids == null || talentUids.size()==0) {
                log.error("talent uids is empty");
            }
        } catch (Exception e) {
            log.error("talent uids load failed", e);
        }
    }

    private static void loadTalent2Cluster(){
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            talent2Cluster  = redis.hgetall("talent_cluster");
            if(talent2Cluster==null || talent2Cluster.size()<=0){
                log.error("talent cluster is empty");
            }
            cluster2talent = talent2Cluster.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        } catch (Exception e) {
            log.error("talent cluster load failed", e);
        }
    }

    private static void loadDiu2Cluster(){
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            diu2Cluster = redis.hgetall("diu_cluster");
            if(diu2Cluster==null || diu2Cluster.size()<=0){
                log.error("diu cluster is empty");
            }
            diu2clusterlist = diu2Cluster.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    item-> Arrays.asList(item.getValue().split(",")),
                    (oldValue,newValue) -> newValue));

        } catch (Exception e) {
            log.error("diu cluster load failed", e);
        }
    }

    private static void loadTalent2Clusterv2(){
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            talent2Cluster_v2  = redis.hgetall("talent_cluster2");
            if(talent2Cluster_v2==null || talent2Cluster_v2.size()<=0){
                log.error("talent cluster2 is empty");
            }
            cluster2talent_v2 = talent2Cluster_v2.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        } catch (Exception e) {
            log.error("talent cluster2 load failed", e);
        }
    }

    private static void loadTalentCF(){
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            talentCF = redis.hgetall("talentCF");
            if(talentCF==null || talentCF.size()<=0){
                log.error("talent CF is empty");
            }

            talentCFlist = talentCF.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    item-> Arrays.asList(item.getValue().split(",")),
                    (oldValue,newValue) -> newValue));
        } catch (Exception e){
            log.error("talent CF load failed", e);
        }
    }

    private static void loadTalentClusterWeight(){
        try {
            RedisClientSingleton redis = RedisClientSingleton.general;
            String value = redis.get("talentClusterWeight");
            String[] splits = value.split("\t");
            if (splits.length < 2) {
                log.error("talentCluster weight is empty");
            }

            clusterUidWeight = Double.parseDouble(splits[0]);
            retrieveKeyweight = Double.parseDouble(splits[1]);

        } catch (Exception e){
            log.error("talent cluster weight load failed", e);
        }
    }
    public static List<String> get() {
        return talentUids;
    }

    public static Map<String, String> getTalent2Cluster(){ return talent2Cluster; }

    public static Map<String, List<String>> getCluster2talent() { return cluster2talent; }

    public static Map<String, String> getDiu2Cluster() { return diu2Cluster; }

    public static Map<String, List<String>> getDiu2clusterlist(){ return diu2clusterlist; }

    public static Map<String, String> getTalent2Cluster_v2(){ return talent2Cluster_v2; }

    public static Map<String, List<String>> getCluster2talent_v2() { return cluster2talent_v2; }

    public static Map<String, String> getTalentCF() { return talentCF; }

    public static Map<String, List<String>> getTalentCFlist(){ return talentCFlist; }

    public static double getClusterUidWeight(){ return clusterUidWeight; }

    public static double getRetrieveKeyweight(){ return retrieveKeyweight; }

    public static void main(String[] args){

    }
}
