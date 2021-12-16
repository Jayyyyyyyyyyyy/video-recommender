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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by admin on 2017/12/2.
 */
public class NewUserRecomeRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private BetaDistribution bd = new BetaDistribution(1, 3);
    ImmutableMap<String, String> aliasMap = ImmutableMap.of(
            "f", "firstcat.tagid",
            "s", "secondcat.tagid",
            "t", "content_tag.tagid"
    );

    public NewUserRecomeRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        for (RetrieverType.Recome type : RetrieverType.Recome.values()) {
            String[] keys = type.getKey().split(",");
            if (type == RetrieverType.Recome.vrecome_eu) {
                int randomIndex = ThreadLocalRandom.current().nextInt(keys.length);
                String sample = keys[randomIndex];
                String key = sample.substring(1);
                String alias = aliasMap.get(sample.substring(0, 1));
                buildRetrieverKey(retrieveKeyContext, type.name(), alias, key);
            } else {
                for (String key : keys) {
                    buildRetrieverKey(retrieveKeyContext, type.name(), type.getAlias(), key.trim());
                }
                //teaserId 渠道广告的推广类别id，可能是一二级类也可是tag，三个都召，只有一个成功 渠道来的用户，自带兴趣点
                String teaserId = recommendContext.getRecommendRequest().getSecondCatId();
                buildRetrieverKey(retrieveKeyContext, type.name(), "firstcat.tagid", teaserId);
                buildRetrieverKey(retrieveKeyContext, type.name(), "secondcat.tagid", teaserId);
                buildRetrieverKey(retrieveKeyContext, type.name(), "content_tag.tagid", teaserId);
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
