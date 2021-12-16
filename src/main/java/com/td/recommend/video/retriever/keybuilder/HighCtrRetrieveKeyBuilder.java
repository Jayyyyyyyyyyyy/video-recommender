package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/9/6.
 */
public class HighCtrRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public HighCtrRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        RetrieveKey retrievekey = new RetrieveKey();
        retrievekey.setType(RetrieverType.vhighctr.name())
                .setKey(recommendContext.getRecommendRequest().getCid());

        retrievekey.setScore(1.0);
        retrieveKeyContext.addRetrieveKey(retrievekey);
    }
}
