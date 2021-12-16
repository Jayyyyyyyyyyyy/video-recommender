package com.td.recommend.video.retriever.keybuilder;

import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;

import java.util.List;

public class BertRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public BertRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> clicked = recommendContext.getClicks();
        List<String> recentClicked = clicked.subList(0, Math.min(3, clicked.size()));
        int maxCnt = 5;
        String retrieveKeyName = RetrieverType.vbert.name();
        String retrieveKeyAlias = RetrieverType.vbert.alias();

        for (String docId : recentClicked) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(retrieveKeyName)
                    .setAlias(retrieveKeyAlias)
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", maxCnt);
            String bucket = recommendContext.hasBucket("exercise_filter-yes") ? "exercise_filter-yes" : "";
            retrieveKey.setPlaceholder(bucket);

            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}

