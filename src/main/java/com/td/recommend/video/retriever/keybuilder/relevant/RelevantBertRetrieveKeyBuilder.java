package com.td.recommend.video.retriever.keybuilder.relevant;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

public class RelevantBertRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public RelevantBertRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        String vid = recommendContext.getRecommendRequest().getVid();
        RetrieveKey retrieveKey = new RetrieveKey();

        retrieveKey.setType(RetrieverType.vbert_rlvt.name())
                .setAlias(RetrieverType.vbert_rlvt.alias())
                .setScore(1.0)
                .setKey(vid);
        if (recommendContext.hasBucket("relevant_exercise_filter-yes")) {
            retrieveKey.setPlaceholder("relevant_exercise_filter-yes");
        }
        retrieveKeyContext.addRetrieveKey(retrieveKey);

    }

}

