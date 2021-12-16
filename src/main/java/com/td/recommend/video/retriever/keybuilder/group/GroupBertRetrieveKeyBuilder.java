package com.td.recommend.video.retriever.keybuilder.group;

import com.td.recommend.commons.profile.UserProfileUtils;
import com.td.recommend.commons.retriever.RetrieveKey;
import com.td.recommend.commons.retriever.RetrieveKeyContext;
import com.td.recommend.userstore.data.UserItem;
import com.td.recommend.video.recommender.VideoRecommenderContext;
import com.td.recommend.video.retriever.RetrieverType;
import com.td.recommend.video.retriever.keybuilder.RetrieveKeyBuilder;

import java.util.List;

public class GroupBertRetrieveKeyBuilder implements RetrieveKeyBuilder {
    private VideoRecommenderContext recommendContext;
    private static final int MAX_COUNT = 50;
    private static final int MAX_ELAPSED_SECONDS = 60 * 60 * 6;

    public GroupBertRetrieveKeyBuilder(VideoRecommenderContext recommendContext) {
        this.recommendContext = recommendContext;
    }

    @Override
    public void build(UserItem userItem, RetrieveKeyContext retrieveKeyContext) {
        List<String> clicked = recommendContext.getClicks();
        List<String> recentClicked = clicked.subList(0, Math.min(3, clicked.size()));
        int maxCnt = 500;
        String retrieveKeyName = RetrieverType.gbert.name();
        String retrieveKeyAlias = RetrieverType.gbert.alias();

        for (String docId : recentClicked) {
            RetrieveKey retrieveKey = new RetrieveKey();
            retrieveKey.setType(retrieveKeyName)
                    .setAlias(retrieveKeyAlias)
                    .setScore(1.0)
                    .setKey(docId)
                    .addAttribute("maxCnt", maxCnt);
            if (UserProfileUtils.isFitnessUser(userItem, null)) {
                retrieveKey.setPlaceholder("1007");
            } else if (UserProfileUtils.is32StepUser(userItem, null)) {
                retrieveKey.setPlaceholder("268");
            }
            retrieveKeyContext.addRetrieveKey(retrieveKey);
        }
    }
}

