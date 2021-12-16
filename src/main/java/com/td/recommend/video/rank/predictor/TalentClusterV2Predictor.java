package com.td.recommend.video.rank.predictor;

import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.featurestore.item.Items;
import com.td.recommend.commons.item.PredictItem;
import com.td.recommend.commons.item.PredictItems;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.core.ranker.IPredictor;
import com.td.recommend.core.ranker.PredictResult;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.video.datasource.BoostWeightMap;
import com.td.recommend.video.utils.TalentUids;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TalentClusterV2Predictor implements IPredictor<DocItem> {
    private IPredictor<DocItem> predictor;

    public TalentClusterV2Predictor(IPredictor<DocItem> predictor) {
        this.predictor = predictor;
    }

    @Override
    public PredictResult predict(PredictItems<DocItem> predictItems, Items queryItems) {
        PredictResult predictResult = predictor.predict(predictItems, queryItems);
        Optional<IItem> contextItemOpt = queryItems.get(ItemKey.context);
        Set<String> talentClusterUids = Collections.emptySet();
        if (contextItemOpt.isPresent()) {
            IItem contextItem = contextItemOpt.get();
            talentClusterUids = getClusterUids(contextItem);
        }
        List<PredictItem<DocItem>> items = predictItems.getItems();
        for (int i = 0; i < items.size(); i++) {
            PredictItem<DocItem> predictItem = items.get(i);
            String uid = DocProfileUtils.getUid(predictItem.getItem());
            double weight = BoostWeightMap.getTalentUidMap().getOrDefault(uid, 1.0);
            double weight2 = BoostWeightMap.getTalentUids_lt3().getOrDefault(uid,1.0);
            double weight3 = 1.0;
            double weight4 = 1.0;

            if(talentClusterUids.contains(uid)){
                weight3 = TalentUids.getClusterUidWeight();
            }
            if(predictItem.getRetrieveKeys().stream().map(RetrieveKey::getType).anyMatch(k -> k.startsWith("vtalentcluster"))){
                //weight4 = 1.5;
                weight4 = TalentUids.getRetrieveKeyweight();
            }
            predictItem.addAttribute("vtalentclusterboost", weight +"*"+ weight2 +"*"+ weight3 + "*" + weight4);
            Double weightedScore = predictResult.getScores().get(i) * weight * weight2 * weight3 * weight4;
            predictResult.getScores().set(i, weightedScore);

        }
        return predictResult;
    }

    private static Set<String> getClusterUids(IItem userContextItem){
        Map<String, String> itemTags = userContextItem.getTags();
        Set<String> talentClusterUids = new HashSet<>();

        if(!itemTags.isEmpty()){
            Map<String, Integer> clusterMap = new HashMap<>();
            try{
                String clusterline = itemTags.getOrDefault("talentcluster","");

                List<String> clusterlist = Arrays.asList(clusterline.split(","));
                clusterlist.forEach(line -> {
                    String[] token = line.split(":");
                    if(token.length==2){
                        clusterMap.put(token[0],Integer.parseInt(token[1]));
                    }
                });
                AtomicInteger valid_cluster_count = new AtomicInteger();
                clusterMap.entrySet().stream().
                        filter(entry -> entry.getValue()>=3).
                        sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                        forEach( entry ->{
                            String cluster = entry.getKey();
                            List<String> uidlist = TalentUids.getCluster2talent_v2().getOrDefault(cluster, new ArrayList<>(0));
                            int cluster_size = entry.getValue();
                            int uidlist_size = uidlist.size();
                            //如果关注的uid在聚类中大约整体聚类的0.333, 或者排序后的第一个聚类
                            if(uidlist_size>0 && ((double) cluster_size /uidlist_size >=0.3 || valid_cluster_count.get()==0)){
                                valid_cluster_count.addAndGet(1);
                                talentClusterUids.addAll(uidlist);
                            }
                        });

                }catch (Exception e){
                e.printStackTrace();
            }
        }
        return talentClusterUids;
    }
}
