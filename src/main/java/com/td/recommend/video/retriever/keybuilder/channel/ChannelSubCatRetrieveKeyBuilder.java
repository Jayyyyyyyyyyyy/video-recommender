package com.td.recommend.video.retriever.keybuilder.channel;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by admin on 2017/6/19.
 */
public class ChannelSubCatRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public ChannelSubCatRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String subCatId = recommendContext.getRecommendRequest().getCid();
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(RetrieverType.vsubcat_chnl.name())
                .setAlias(RetrieverType.vsubcat_chnl.alias())
                .setKey(subCatId);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
