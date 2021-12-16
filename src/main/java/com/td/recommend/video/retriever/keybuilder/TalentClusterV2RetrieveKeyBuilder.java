package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.utils.RedisClientSingleton;
import com.td.recommend.video.utils.TalentUids;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by sunjian on 2021/09/18.
 */
public class TalentClusterV2RetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final Logger log = LoggerFactory.getLogger(TalentClusterV2RetrieveKeyBuilder.class);

    public TalentClusterV2RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        //1）获取xfollow author兴趣点 的达人，然后聚类
        //2) 如果有聚类，某个聚类个数大于等于3个，就出这些聚类
        //3) 如果没有聚类，走talentCF 方式 去找follow中的相似达人，然后按照去重之后去召回

        int ihf = recommendContext.getRecommendRequest().getIhf();
        UserItem userItem_diu = recommendContext.getUserItem();

        List<String> xfollowUids = new ArrayList<>(UserProfileUtils.getValueFeaturesMap(userItem_diu, "xfollow").keySet());

        List<String> st_uid_cs = new ArrayList<>(UserProfileUtils.getVarianceFeatureMap(userItem_diu, "st_vauthor_uid_cs").keySet());

        List<String> uid_cs = new ArrayList<>(UserProfileUtils.getVarianceFeatureMap(userItem_diu, "vauthor_uid_cs").keySet());

        List<String> st_uid_ck = UserProfileUtils.getVarianceFeatureMap(userItem_diu, "st_vauthor_uid_ck").entrySet().stream().
                filter(entry -> entry.getValue().getMean()>0.0 && entry.getValue().getVariance()<0.9).
                map(Map.Entry::getKey).collect(Collectors.toList());

        List<String> uid_ck = UserProfileUtils.getVarianceFeatureMap(userItem_diu, "vauthor_uid_ck").entrySet().stream().
                filter(entry -> entry.getValue().getMean()>0.0 && entry.getValue().getVariance()<0.9 ).
                map(Map.Entry::getKey).collect(Collectors.toList());

        Map<String, List<String>> key2Uids = new LinkedHashMap<>();
        key2Uids.put("xfollow", xfollowUids);
        key2Uids.put("st_vauthor_uid_cs", st_uid_cs);
        key2Uids.put("vauthor_uid_cs", uid_cs);
        key2Uids.put("st_vauthor_uid_ck", st_uid_ck);
        key2Uids.put("vauthor_uid_ck", uid_ck);

        Map<String, Set<String>> my_cluster = new HashMap<>();
        Map<String, List<String>> cfmap = new HashMap<>();

        boolean hasCluster = false;
        for(Map.Entry<String,List<String>> entry: key2Uids.entrySet()){
            if(!hasCluster){
                hasCluster = createCluster(entry.getValue(), my_cluster, cfmap, 5, 3);
            }
        }
        if(hasCluster){
            AtomicInteger valid_cluster_count = new AtomicInteger();
            my_cluster.entrySet().stream().
                    filter(entry -> entry.getValue().size()>=3)
                    .sorted(new Comparator<Map.Entry<String, Set<String>>>() {
                        @Override
                        public int compare(Map.Entry<String, Set<String>> o1, Map.Entry<String, Set<String>> o2) {
                            return Integer.compare(o2.getValue().size(), o1.getValue().size());
                        }
                    })
                    .forEach(entry ->{
                        String cluster = entry.getKey();
                        List<String> uidlist = TalentUids.getCluster2talent_v2().getOrDefault(cluster, new ArrayList<>(0));
                        int cluster_size = entry.getValue().size();
                        int uidlist_size = uidlist.size();

                        if(uidlist_size>0 && ((double) cluster_size /uidlist_size >=0.3 || valid_cluster_count.get()==0)){

                            valid_cluster_count.addAndGet(1);
                            for(String cuid: uidlist){
                                if(StringUtils.isNumeric(cuid)){
                                    RetrieveKey retrieveKey = new RetrieveKey();
                                    retrieveKey.setIhf(String.valueOf(ihf));
                                    retrieveKey.setType(RetrieverType.vtalentclusterv2.name());
                                    retrieveKey.setAlias(RetrieverType.vtalentclusterv2.alias());
                                    retrieveKey.setKey(cuid);
                                    if(recommendContext.hasBucket("talentclusterv2-yes")){
                                        retrieveKey.setPlaceholder("talentclusterv2-yes");
                                    }
                                    retrieveKeyContext.addRetrieveKey(retrieveKey);
                                }
                            }
                        }
                    });
        }
        else{
            cfmap.values().stream().flatMap(Collection::stream).
                    collect(Collectors.groupingBy(x-> x, Collectors.counting())).forEach((cuid,count) ->{
                        if(StringUtils.isNumeric(cuid) && count>=2){
                            RetrieveKey retrieveKey = new RetrieveKey();
                            retrieveKey.setIhf(String.valueOf(ihf));
                            retrieveKey.setType(RetrieverType.vtalentclusterv2.name());
                            retrieveKey.setAlias(RetrieverType.vtalentclusterv2.alias());
                            retrieveKey.setKey(cuid);
                            if(recommendContext.hasBucket("talentclusterv2-yes")){
                                retrieveKey.setPlaceholder("talentclusterv2-yes");
                            }
                            retrieveKeyContext.addRetrieveKey(retrieveKey);
                        }
            });
        }
    }


    public static boolean createCluster(List<String> intputUids,
                                        Map<String, Set<String>> clusterMap,
                                        Map<String, List<String>> cfMap,
                                        int cfLimit, int clusterLimit){
        intputUids.stream().filter(uid -> TalentUids.getTalent2Cluster_v2().containsKey(uid)).forEach( csuid -> {
            String cluster = TalentUids.getTalent2Cluster_v2().getOrDefault(csuid, "");
            if(StringUtils.isNotBlank(cluster)){
                clusterMap.computeIfAbsent(cluster,k -> new HashSet<>()).add(csuid);
            }
            if(!cfMap.containsKey(csuid)){
                List<String> cflist = TalentUids.getTalentCFlist().getOrDefault(csuid, Collections.emptyList());
                if(!cflist.isEmpty()){
                    cfMap.put(csuid, cflist.subList(0,Math.min(cfLimit, cflist.size())));
                }
            }
        });
        return clusterMap.entrySet().stream().anyMatch(entry -> entry.getValue().size()>=clusterLimit);
    }

    /*
    private static volatile Map<String, String> talent2Cluster_v2 = Collections.emptyMap();
    private static volatile Map<String, List<String>> cluster2talent_v2 = Collections.emptyMap();

    private static volatile Map<String, String> talentCF = Collections.emptyMap();
    private static volatile Map<String, List<String>> talentCFlist = Collections.emptyMap();
    private static volatile Map<String, String> uid2name = Collections.emptyMap();

    public static boolean localCreateCluster(List<String> intputUids,
                                        Map<String, Set<String>> clusterMap,
                                        Map<String, List<String>> cfMap,
                                        int cfLimit, int clusterLimit){
        intputUids.stream().filter(uid -> talent2Cluster_v2.containsKey(uid)).forEach( csuid -> {
            String cluster = talent2Cluster_v2.getOrDefault(csuid, "");
            if(StringUtils.isNotBlank(cluster)){
                clusterMap.computeIfAbsent(cluster,k -> new HashSet<>()).add(csuid);
            }
            if(!cfMap.containsKey(csuid)){
                List<String> cflist = talentCFlist.getOrDefault(csuid, Collections.emptyList());
                if(!cflist.isEmpty()){
                    cfMap.put(csuid, cflist.subList(0,Math.min(cfLimit, cflist.size())));
                }
            }
        });
        return clusterMap.entrySet().stream().anyMatch(entry -> entry.getValue().size()>=clusterLimit);
    }

    public static void main(String[] args){

        RedisClientSingleton redis = RedisClientSingleton.general;
        try {
            talent2Cluster_v2  = redis.hgetall("talent_cluster2");
            if(talent2Cluster_v2==null || talent2Cluster_v2.size()<=0){
                log.error("talent cluster2 is empty");
            }
            cluster2talent_v2 = talent2Cluster_v2.entrySet().stream().collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        } catch (Exception e) {
            log.error("talent cluster2 load failed", e);
        }

        try {
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
        String dictFileName = "/Users/sunjian/Documents/gitlab-git/video-recommender/src/main/profiles/test/cluster_new.txt";
        //ClassLoader classLoader = TalentClusterV2RetrieveKeyBuilder.class.getClassLoader();

        try (BufferedReader br = new BufferedReader(new FileReader(dictFileName))) {
            String line;
            Map<String, String> map = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(":");
                if (fields.length >= 2) {
                    String uid = fields[0];
                    String uname = fields[1];
                    map.put(uid,uname);
                }
            }
            uid2name = map;
        } catch (IOException e) {
            log.error("Read cluster_new from file={} failed!", dictFileName, e);
        }


        //String diu = "e9f0fa2f4afa2895";
        //sunjian
        //String diu = "66916496a54c8bc5";
        String uid = "12283526";

        String diu = "8bd182640ac88b82";
//        String uid = "6423835";
        UserItem userItem_diu = new UserItemDao().get(diu).get();

        List<String> xfollowUids = new ArrayList<>(UserProfileUtils.getValueFeaturesMap(userItem_diu, "xfollow").keySet());

        List<String> st_uid_cs = new ArrayList<>(UserProfileUtils.getVarianceFeatureMap(userItem_diu, "st_vauthor_uid_cs").keySet());

        List<String> uid_cs = new ArrayList<>(UserProfileUtils.getVarianceFeatureMap(userItem_diu, "vauthor_uid_cs").keySet());

        List<String> st_uid_ck = UserProfileUtils.getVarianceFeatureMap(userItem_diu, "st_vauthor_uid_ck").entrySet().stream().
                filter(entry -> entry.getValue().getMean()>0.0 && entry.getValue().getVariance()<0.9).
                map(Map.Entry::getKey).collect(Collectors.toList());

        List<String> uid_ck = UserProfileUtils.getVarianceFeatureMap(userItem_diu, "vauthor_uid_ck").entrySet().stream().
                filter(entry -> entry.getValue().getMean()>0.0 && entry.getValue().getVariance()<0.9 ).
                map(Map.Entry::getKey).collect(Collectors.toList());

        Map<String, List<String>> key2Uids = new LinkedHashMap<>();
        key2Uids.put("xfollow", xfollowUids);
        key2Uids.put("st_vauthor_uid_cs", st_uid_cs);
        key2Uids.put("vauthor_uid_cs", uid_cs);
        key2Uids.put("st_vauthor_uid_ck", st_uid_ck);
        key2Uids.put("vauthor_uid_ck", uid_ck);

        Map<String, Set<String>> my_cluster = new HashMap<>();
        Map<String, List<String>> cfmap = new HashMap<>();

        boolean hasCluster = false;
        String lastKey = "";
        List<String> resultUids = new ArrayList<>();

        for(Map.Entry<String,List<String>> entry: key2Uids.entrySet()){
            if(!hasCluster){
                hasCluster = localCreateCluster(entry.getValue(), my_cluster, cfmap, 5, 3);
                lastKey = entry.getKey();
                System.out.println("<Entry>"+entry+"<Cluster>"+my_cluster +"<CF>"+cfmap);
            }
        }
        System.out.println("----------------------------------------------------------");
        if(hasCluster){
            System.out.println("LastKey:"+lastKey);
            System.out.println("LastCluster:"+my_cluster);

            AtomicInteger valid_cluster_count = new AtomicInteger();
            List<String> finalResultUids = resultUids;
            my_cluster.entrySet().stream().
                    filter(entry -> entry.getValue().size()>=3)
                    .sorted(new Comparator<Map.Entry<String, Set<String>>>() {
                        @Override
                        public int compare(Map.Entry<String, Set<String>> o1, Map.Entry<String, Set<String>> o2) {
                            return Integer.compare(o2.getValue().size(), o1.getValue().size());
                        }
                    })
                    .forEach(entry ->{
                String cluster = entry.getKey();
                List<String> uidlist = cluster2talent_v2.getOrDefault(cluster, new ArrayList<>(0));
                int cluster_size = entry.getValue().size();
                int uidlist_size = uidlist.size();
                if(uidlist_size>0){
                    double ratio = (double) cluster_size /uidlist_size;
                    if(ratio>=0.3 || valid_cluster_count.get()==0){
                        finalResultUids.addAll(uidlist);
                        for(String cuid: uidlist){
                            if(StringUtils.isNumeric(cuid)){
                                String uname = uid2name.getOrDefault(cuid, "");
                                System.out.println(
                                        "cuid="+cuid+"," +
                                                "cluster="+cluster+"," +
                                                "cluster.size=["+entry.getValue().size()+"]"+"," +
                                                "uidlist.size="+String.valueOf(uidlist.size())+"," +
                                                "uname="+uname);
                            }
                        }
                        valid_cluster_count.addAndGet(1);
                    }
                }
            });
        }
        else{
            System.out.println("talentCF");
            resultUids = cfmap.values().stream().flatMap(Collection::stream).
                    collect(Collectors.groupingBy(x-> x, Collectors.counting())).
                    entrySet().stream().filter(entry -> entry.getValue()>=2).
                    sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).
                    collect(Collectors.toList());
            System.out.println(cfmap.values().stream().flatMap(Collection::stream).collect(Collectors.groupingBy(x-> x, Collectors.counting())));
            resultUids.forEach(cuid ->{
                String uname = uid2name.getOrDefault(cuid, "");
                System.out.println("cuid="+cuid+",uname="+uname);
            });
        }
    }
    */

}
