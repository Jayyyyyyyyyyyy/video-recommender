package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;

public class OperatorHotRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private BetaDistribution bd = new BetaDistribution(1, 3);

    public OperatorHotRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        for (RetrieverType.OperatorHot type : RetrieverType.OperatorHot.values()) {
            String[] keys = type.getKey().split(",");
            if (type == RetrieverType.OperatorHot.voperator_eu) {
                String key = keys[(int) (bd.sample() * keys.length)];
                buildRetrieverKey(retrieveKeyContext, type.name(), key.trim());
            } else {
                for (String key : keys) {
                    buildRetrieverKey(retrieveKeyContext, type.name(), key.trim());
                }
            }
        }
    }

    private void buildRetrieverKey(RetrieveKeyContext retrieveKeyContext, String type, String key) {
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(type)
                .setKey(key);
        try {
            UserItem userItem = recommendContext.getUserItem();
            Map<String, String> sValueFeaturesMap = UserProfileUtils.getSValueFeaturesMap(userItem, RetrieverType.vdegree_init.name());
            String degree = sValueFeaturesMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
            retrieveKey.setPlaceholder(degree);
        } catch (Exception ignore) {
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
