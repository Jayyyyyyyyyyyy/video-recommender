package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/12/2.
 */
public class SplitFlowRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public SplitFlowRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String userId = userItem.getId().replaceFirst("debug_", "");
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.vsplitflow.name())
                .setAlias(RetrieverType.vsplitflow.alias())
                .setKey(userId);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
