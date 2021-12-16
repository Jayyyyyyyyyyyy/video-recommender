package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/12/2.
 */
public class TalentRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public TalentRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {

        RetrieveKey retrieveKey = new RetrieveKey();
        int ihf = recommendContext.getRecommendRequest().getIhf();
        retrieveKey.setIhf(String.valueOf(ihf))
                .setType(RetrieverType.vtalent.name())
                .setKey(RetrieverType.vtalent.name())
                .setReason("达人精品");
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
