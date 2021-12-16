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
public class ChannelRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public ChannelRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String channel = recommendContext.getRecommendRequest().getTemplate();//eg: vsubcat_chnl
        String cid = recommendContext.getRecommendRequest().getCid();//eg: 265
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setType(RetrieverType.valueOf(channel).name())
                .setAlias(RetrieverType.valueOf(channel).alias())
                .setKey(cid);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
