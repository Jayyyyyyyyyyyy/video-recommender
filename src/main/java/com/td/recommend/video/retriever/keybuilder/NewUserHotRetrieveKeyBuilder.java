package com.td.recommend.video.retriever.keybuilder;

import com.google.common.collect.ImmutableMap;
import com.td.featurestore.item.IItem;
import com.td.featurestore.item.ItemKey;
import com.td.recommend.commons.profile.DocProfileUtils;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.docstore.data.DocItem;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.BetaDistribution;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by admin on 2017/12/2.
 */
public class NewUserHotRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private BetaDistribution bd = new BetaDistribution(1, 3);
    ImmutableMap<String, String> aliasMap = ImmutableMap.of(
            "f", "firstcat.tagid",
            "s", "secondcat.tagid",
            "t", "content_tag.tagid"
    );

    public NewUserHotRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        for (RetrieverType.Hot type : RetrieverType.Hot.values()) {
            String[] keys = type.getKey().split(",");
            if (type == RetrieverType.Hot.vhot_eu) {
                int randomIndex = ThreadLocalRandom.current().nextInt(keys.length);
                String sample = keys[randomIndex];
                String key = sample.substring(1);
                String alias = aliasMap.get(sample.substring(0, 1));
                buildRetrieverKey(retrieveKeyContext, type.name(), alias, key);
            } else {
                for (String key : keys) {
                    buildRetrieverKey(retrieveKeyContext, type.name(), type.getAlias(), key.trim());
                }
            }
        }

        //teaserId 广告素材的推广类别id，可能是一二级类也可是tag，三个都召，只有一个成功
        String teaserId = recommendContext.getRecommendRequest().getSecondCatId();
        if(StringUtils.isNotBlank(teaserId)) {
            buildRetrieverKey(retrieveKeyContext, "vcat_hot", "firstcat.tagid", teaserId);
            buildRetrieverKey(retrieveKeyContext, "vsubcat_hot", "secondcat.tagid", teaserId);
            buildRetrieverKey(retrieveKeyContext, "vtag_hot", "content_tag.tagid", teaserId);
        }
        //广告素材vid画像，二级类加一路
        Optional<IItem> teaserItem = recommendContext.getQueryItems().get(ItemKey.interest);
        if (teaserItem.isPresent()) {
            String secondCat = DocProfileUtils.getSecondCat((DocItem) teaserItem.get());
            buildRetrieverKey(retrieveKeyContext, "vsubcat_hot", "secondcat.tagid", secondCat);
        }
    }

    private void buildRetrieverKey(RetrieveKeyContext retrieveKeyContext, String type, String alias, String key) {
        int ihf = recommendContext.getRecommendRequest().getIhf();
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setIhf(String.valueOf(ihf))
                .setType(type)
                .setKey(key)
                .setAlias(alias);
        if (recommendContext.hasBucket("newuser_strategy-yes")) {
            retrieveKey.setPlaceholder("newuser_strategy-yes");
        }

        if (recommendContext.getUserInterest().equals("264")) {
            try {
                UserItem userItem = recommendContext.getUserItem();
                Map<String, String> sValueFeaturesMap = UserProfileUtils.getSValueFeaturesMap(userItem, RetrieverType.vdegree_init.name());
                String degree = sValueFeaturesMap.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey();
                retrieveKey.setCategory(degree);
            } catch (Exception ignore) {
            }
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
