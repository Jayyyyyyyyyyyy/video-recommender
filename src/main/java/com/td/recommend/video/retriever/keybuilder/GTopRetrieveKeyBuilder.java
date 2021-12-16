package com.td.recommend.video.retriever.keybuilder;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/8/8.
 */
public class GTopRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public GTopRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;

    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        for (RetrieverType.GTop type : RetrieverType.GTop.values()) {
            TVariance variance = UserProfileUtils.getVarianceFeatureMap(userItem, type.getFacet()).get(type.getKey());
            if (variance != null && variance.mean > 0.5) {
                RetrieveKey retrievekey = new RetrieveKey();
                retrievekey.setType(type.name());
                retrievekey.setAlias(type.getAlias());
                retrievekey.setKey(type.getKey());
                retrievekey.addAttribute("maxCnt", 10);
                retrievekey.setReason(type.getReason());
                retrieveKeyContext.addRetrieveKey(retrievekey);
            }
        }
    }
}
