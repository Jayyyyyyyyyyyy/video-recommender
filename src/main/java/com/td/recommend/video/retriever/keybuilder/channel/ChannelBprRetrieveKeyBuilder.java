package com.td.recommend.video.retriever.keybuilder.channel;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/12/2.
 */
public class ChannelBprRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public ChannelBprRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }
    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String userId = userItem.getId().replaceFirst("debug_", "");

        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.vbpr_chnl.name())
                .setAlias(RetrieverType.vbpr_chnl.alias())
                .setKey(userId);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
