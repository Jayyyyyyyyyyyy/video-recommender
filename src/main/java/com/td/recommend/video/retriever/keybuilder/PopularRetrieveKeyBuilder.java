package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * create by pansm at 2019/08/30
 */
public class PopularRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public PopularRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        RetrieveKey retrievekey = new RetrieveKey();
        retrievekey.setType(RetrieverType.vpopular.name())
                .setKey(recommendContext.getRecommendRequest().getCid())
                .setReason("最新流行");
        String bucket = recommendContext.hasBucket("exercise_filter-yes") ? "exercise_filter-yes" : "";
        retrievekey.setPlaceholder(bucket);
        retrievekey.setScore(1.0);
        retrieveKeyContext.addRetrieveKey(retrievekey);
    }
}
