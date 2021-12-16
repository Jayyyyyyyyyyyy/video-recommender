package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/12/2.
 */
public class RelevantYoutubeDNNRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    public RelevantYoutubeDNNRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String userId = userItem.getId();


        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.vyoutubednn_rlvt.name())
                .setAlias(RetrieverType.vyoutubednn_rlvt.alias())
                .setKey(userId);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
