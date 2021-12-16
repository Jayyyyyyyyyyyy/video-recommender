package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

/**
 * Created by admin on 2017/12/2.
 */
public class ClusterRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;

    public ClusterRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String bucket = recommendContext.getBuckets().stream()
                .filter(i -> i.startsWith("vcluster")).findFirst()
                .map(i -> i.split("-")[1]).orElse("base");

        String userId = userItem.getId().replaceFirst("debug_", "");
        RetrieveKey retrieveKey = new RetrieveKey();
        retrieveKey.setScore(1.0)
                .setType(RetrieverType.vcluster.name())
                .setAlias(RetrieverType.vcluster.alias())
                .setKey(userId);
        retrieveKey.setPlaceholder(bucket);
        retrieveKeyContext.addRetrieveKey(retrieveKey);
    }
}
