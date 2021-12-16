package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.request.Ihf;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.utils.TalentUids;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by sunjian on 2021/09/12.
 */
public class TalentClusterRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public TalentClusterRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
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
                        List<String> uidlist = TalentUids.getCluster2talent().getOrDefault(cluster, new ArrayList<>(0));
                        int cluster_size = entry.getValue().size();
                        int uidlist_size = uidlist.size();

                        if(uidlist_size>0 && ((double) cluster_size /uidlist_size >=0.3 || valid_cluster_count.get()==0)){

                            valid_cluster_count.addAndGet(1);
                            for(String cuid: uidlist){
                                if(StringUtils.isNumeric(cuid)){
                                    RetrieveKey retrieveKey = new RetrieveKey();
                                    retrieveKey.setIhf(String.valueOf(ihf));
                                    retrieveKey.setType(RetrieverType.vtalentcluster.name());
                                    retrieveKey.setAlias(RetrieverType.vtalentcluster.alias());
                                    retrieveKey.setKey(cuid);
                                    if(recommendContext.hasBucket("talentcluster-yes")){
                                        retrieveKey.setPlaceholder("talentcluster-yes");
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
                    retrieveKey.setType(RetrieverType.vtalentcluster.name());
                    retrieveKey.setAlias(RetrieverType.vtalentcluster.alias());
                    retrieveKey.setKey(cuid);
                    if(recommendContext.hasBucket("talentcluster-yes")){
                        retrieveKey.setPlaceholder("talentcluster-yes");
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
        intputUids.stream().filter(uid -> TalentUids.getTalent2Cluster().containsKey(uid)).forEach( csuid -> {
            String cluster = TalentUids.getTalent2Cluster().getOrDefault(csuid, "");
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
}
