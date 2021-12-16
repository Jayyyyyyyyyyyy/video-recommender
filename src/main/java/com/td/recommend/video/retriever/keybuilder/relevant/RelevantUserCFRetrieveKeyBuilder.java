package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/8/8.
 */
public class RelevantUserCFRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    public RelevantUserCFRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String diu = userItem.getId();

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.vusercf_rlvt.name())
                .setAlias(RetrieverType.vusercf_rlvt.alias())
                .setKey(diu);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
