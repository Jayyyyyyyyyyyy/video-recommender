package com.td.recommend.video.retriever.keybuilder.trend;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

/**
 * Created by sunjian on 2021/09/09.
 */
public class THotRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public THotRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        RetrieveKey retrieveKey = new RetrieveKey();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        //目前只限在社区首页里build
        retrieveKey.setIhf(String.valueOf(ihf))
                .setType(RetrieverType.thot.name())
                .setKey(RetrieverType.thot.name());

        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
