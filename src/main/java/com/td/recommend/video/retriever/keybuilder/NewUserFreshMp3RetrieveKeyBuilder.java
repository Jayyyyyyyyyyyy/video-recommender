package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.ImmutableMap;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;

/**
 * Created by admin on 2017/12/2.
 */
public class NewUserFreshMp3RetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private BetaDistribution bd = new BetaDistribution(1, 3);
    ImmutableMap<String, String> aliasMap = ImmutableMap.of(
            "f", "firstcat.tagid",
            "s", "secondcat.tagid",
            "t", "content_tag.tagid"
    );

    public NewUserFreshMp3RetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        for (RetrieverType.FreshMp3 type : RetrieverType.FreshMp3.values()) {
            String[] keys = type.getKey().split(",");
            for (String key : keys) {
                buildRetrieverKey(retrieveKeyContext, type.name(), type.getAlias(), key.trim());
            }
        }
    }

    private void buildRetrieverKey(RetrieveKeyContext retrieveKeyContext, String type, String alias, String key) {
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(type)
                .setKey(key)
                .setAlias(alias);
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
