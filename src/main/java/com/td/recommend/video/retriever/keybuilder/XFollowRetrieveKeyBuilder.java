package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.datasource.UserVideoItemDataSource;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.*;

/**
 * Created by admin on 2017/12/2.
 */
public class XFollowRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public XFollowRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, RetrieverType.vxfollow);
        featureExtractor.extractL2Map(Collections.singletonList("xfollow"));
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();
        String lastFollow = "";
        try {
            List<String> Ids = userItem.getUserRawData().get()
                    .getFeaturesMap().get("xfollow").getKey();
            lastFollow = Ids.get(0);
        } catch (Exception ignored) {
        }
        String key = lastFollow;

        Set<RetrieveKey> interests = new HashSet<>();
        Set<RetrieveKey> nonSample = new HashSet<>();
        retrieveKeys.forEach(k -> {
            if (k.getType().matches(".*(_ev|_tr|_op|_live|_f3)|vx.*")) {
                nonSample.add(k);
            } else {
                interests.add(k);
            }
        });

        double p = Math.min(1.0, 3 / (interests.size() + 0.1));
        BinomialDistribution bd = new BinomialDistribution(1, p);
        interests.stream()
                .filter(r -> bd.sample() == 1 || r.getKey().equals(key))
                .forEach(retrieveKeyContext::addRetrieveKey);
        nonSample.forEach(retrieveKeyContext::addRetrieveKey);
    }

    public static void main(String[] args) {

//        String diu = "66916496a54c8bc5";
//        String diu = "863525049525340";
        UserItem userItem_diu = new UserItemDao().get("e9f0fa2f4afa2895").get();
        Map<String, Double> featureMap_diu = UserProfileUtils.getValueFeaturesMap(userItem_diu, "xfollow");
        for (Map.Entry<String, Double> entry : featureMap_diu.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        UserItem user_uid = UserVideoItemDataSource.getInstance().getUserItemDao().getUserFollow("12283526").get();
        Map<String, Double> featureMap_uid = UserProfileUtils.getValueFeaturesMap(user_uid, "tfollow");

        for (Map.Entry<String, Double> entry : featureMap_uid.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
//
//
//        UserItem userItem_uid = new UserItemDao().get("12283526").get();
//
//
//        UserItem userItem_diu = new UserItemDao().get("66916496a54c8bc5").get();
//
//        UserItem user = UserVideoItemDataSource.getInstance().getUserItemDao().getUserFollow("863525049525340").get();
//        UserItem user_uid = UserVideoItemDataSource.getInstance().getUserItemDao().getUserFollow("12283526").get();
//        UserItem user_diu = UserVideoItemDataSource.getInstance().getUserItemDao().getUserFollow("66916496a54c8bc5").get();
//        //Map<String, Double> featureMap = UserProfileUtils.getValueFeaturesMap(user, "vfollow");
//
////        TL2Entry follow = userItem.getUserRawData().get().getFeaturesMap().get("xfollow");
////        System.out.println(follow.getKey()+":"+follow.getSvalue());
//        Map<String, Double> featureMap = UserProfileUtils.getValueFeaturesMap(userItem, "tfollow");
//        Map<String, Double> featureMap_uid = UserProfileUtils.getValueFeaturesMap(user_uid, "vfollow");
//        Map<String, Double> featureMap_diu = UserProfileUtils.getValueFeaturesMap(userItem_diu, "tfollow");
//        for (Map.Entry<String, Double> entry : featureMap.entrySet()) {
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }
    }
}
