package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.dao.UserItemDao;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.keyfeatures.InterestFeatureExtractor;
import org.apache.commons.math3.distribution.BinomialDistribution;

import java.util.*;

/**
 * Created by admin on 2017/12/2.
 */
public class IFollowRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public IFollowRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        InterestFeatureExtractor featureExtractor = new InterestFeatureExtractor(recommendContext, RetrieverType.vifollow);
        featureExtractor.extractL2Map(Collections.singletonList("ifollow"));
        Set<RetrieveKey> retrieveKeys = featureExtractor.getKeyFeaturesMap().keySet();
        String lastFollow = "";
        try {
            List<String> Ids = userItem.getUserRawData().get()
                    .getFeaturesMap().get("ifollow").getKey();
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
        UserItem userItem = new UserItemDao().get("c4538004cffce1f5").get();
        Map<String, String> featureMap = UserProfileUtils.getSValueFeaturesMap(userItem, "ifollow");
        for (Map.Entry<String, String> entry : featureMap.entrySet()) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
    }
}
