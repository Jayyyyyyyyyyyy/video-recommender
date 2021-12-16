package com.td.recommend.video.retriever.keybuilder.group;

import com.td.data.profile.TVariance;
import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

import java.util.Map;

/**
 * Created by admin on 2017/6/21.
 */
public class GroupSubcatRetrieveKeyBuilder implements RetrieveKeyBuilder {
    public VideoRecommenderContext recommendContext;

    public GroupSubcatRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        Map<String, TVariance> group = UserProfileUtils.getVarianceFeatureMap(userItem, "group_subcat");
        RetrieverType type = RetrieverType.gsubcat;
        group.forEach((k, v) -> {
            if (v != null && v.mean > 0.5) {
                RetrieveKey retrievekey = new RetrieveKey();
                retrievekey.setType(type.name());
                retrievekey.setAlias(type.alias());
                retrievekey.setKey(k);
                retrieveKeyContext.addRetrieveKey(retrievekey);
            }
        });
    }
}